import { vi } from 'vitest';
import NotificationService from '../notificationService';

// Mock SockJS and STOMP
const mockSubscribe = vi.fn();
const mockUnsubscribe = vi.fn();
const mockPublish = vi.fn();
const mockActivate = vi.fn();
const mockDeactivate = vi.fn();

const mockStompClient = {
  subscribe: mockSubscribe,
  publish: mockPublish,
  activate: mockActivate,
  deactivate: mockDeactivate,
};

vi.mock('sockjs-client', () => {
  return {
    default: vi.fn(() => ({
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    })),
  };
});

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn(() => mockStompClient),
}));

// Mock Notification API
Object.defineProperty(window, 'Notification', {
  value: class MockNotification {
    constructor(title, options) {
      this.title = title;
      this.options = options;
    }
    static permission = 'granted';
    static requestPermission = vi.fn(() => Promise.resolve('granted'));
  },
  configurable: true,
});

describe('NotificationService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSubscribe.mockReturnValue({ unsubscribe: mockUnsubscribe });
    NotificationService.connected = false;
    NotificationService.stompClient = null;
    NotificationService.subscriptions.clear();
  });

  describe('Connection Management', () => {
    test('connects to WebSocket with correct configuration', () => {
      const userId = 1;
      const onNotification = vi.fn();

      NotificationService.connect(userId, onNotification);

      expect(mockActivate).toHaveBeenCalled();
      expect(NotificationService.stompClient).toBe(mockStompClient);
    });

    test.skip('handles successful connection', () => {
      const userId = 1;
      const onNotification = vi.fn();
      const mockMessage = {
        body: JSON.stringify({
          id: 1,
          type: 'WEATHER_ALERT',
          message: 'Test notification',
        }),
      };

      // Mock the Client constructor to immediately call onConnect
      const { Client } = require('@stomp/stompjs');
      Client.mockImplementation((config) => {
        // Simulate successful connection
        setTimeout(() => {
          config.onConnect();
        }, 0);
        return mockStompClient;
      });

      NotificationService.connect(userId, onNotification);

      // Wait for the connection to be established
      setTimeout(() => {
        expect(NotificationService.connected).toBe(true);
        expect(mockSubscribe).toHaveBeenCalledWith(
          '/queue/notifications',
          expect.any(Function)
        );

        // Test notification handling
        const subscribeCallback = mockSubscribe.mock.calls[0][1];
        subscribeCallback(mockMessage);
        
        expect(onNotification).toHaveBeenCalledWith({
          id: 1,
          type: 'WEATHER_ALERT',
          message: 'Test notification',
        });
      }, 10);
    });

    test.skip('handles connection errors', () => {
      const userId = 1;
      const onNotification = vi.fn();
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      const { Client } = require('@stomp/stompjs');
      Client.mockImplementation((config) => {
        setTimeout(() => {
          config.onStompError({ message: 'Connection failed' });
        }, 0);
        return mockStompClient;
      });

      NotificationService.connect(userId, onNotification);

      setTimeout(() => {
        expect(consoleSpy).toHaveBeenCalledWith('❌ STOMP error:', { message: 'Connection failed' });
      }, 10);

      consoleSpy.mockRestore();
    });

    test('disconnects properly', () => {
      const userId = 1;
      const onNotification = vi.fn();

      // Setup connection first
      NotificationService.connect(userId, onNotification);
      NotificationService.connected = true;
      NotificationService.stompClient = mockStompClient;
      NotificationService.subscriptions.set(userId, { unsubscribe: mockUnsubscribe });

      // Disconnect
      NotificationService.disconnect();

      expect(mockUnsubscribe).toHaveBeenCalled();
      expect(mockDeactivate).toHaveBeenCalled();
      expect(NotificationService.connected).toBe(false);
      expect(NotificationService.subscriptions.size).toBe(0);
    });

    test.skip('handles disconnect event', () => {
      const userId = 1;
      const onNotification = vi.fn();

      const { Client } = require('@stomp/stompjs');
      Client.mockImplementation((config) => {
        setTimeout(() => {
          config.onDisconnect();
        }, 0);
        return mockStompClient;
      });

      NotificationService.connect(userId, onNotification);

      setTimeout(() => {
        expect(NotificationService.connected).toBe(false);
        expect(NotificationService.subscriptions.size).toBe(0);
      }, 10);
    });
  });

  describe('Notification Publishing', () => {
    test('sends test notification when connected', () => {
      NotificationService.connected = true;
      NotificationService.stompClient = mockStompClient;

      const userId = 1;
      const message = 'Test notification';

      NotificationService.sendTestNotification(userId, message);

      expect(mockPublish).toHaveBeenCalledWith({
        destination: '/app/notify',
        body: JSON.stringify({
          userId: userId,
          message: message,
          type: 'WEATHER_ALERT',
        }),
      });
    });

    test('does not send notification when not connected', () => {
      NotificationService.connected = false;
      NotificationService.stompClient = mockStompClient;

      const userId = 1;
      const message = 'Test notification';

      NotificationService.sendTestNotification(userId, message);

      expect(mockPublish).not.toHaveBeenCalled();
    });

    test('does not send notification when no client', () => {
      NotificationService.connected = true;
      NotificationService.stompClient = null;

      const userId = 1;
      const message = 'Test notification';

      NotificationService.sendTestNotification(userId, message);

      expect(mockPublish).not.toHaveBeenCalled();
    });
  });

  describe('Browser Notifications', () => {
    let NotificationSpy;

    beforeEach(() => {
      // Mock Notification constructor
      NotificationSpy = vi.fn().mockImplementation((title, options) => ({
        title,
        ...options
      }));
      
      // Use Object.defineProperty to avoid read-only issues
      Object.defineProperty(window, 'Notification', {
        value: NotificationSpy,
        writable: true,
        configurable: true,
      });
      
      window.Notification.permission = 'granted';
      window.Notification.requestPermission = vi.fn().mockResolvedValue('granted');
    });

    test('shows browser notification when permission granted', () => {
      const notification = {
        title: 'Test Title',
        message: 'Test message',
      };

      NotificationService.showBrowserNotification(notification);

      expect(window.Notification).toHaveBeenCalledWith('Test Title', {
        body: 'Test message',
        icon: '/favicon.ico',
        badge: '/favicon.ico',
      });
    });

    test('does not show browser notification when permission denied', () => {
      const notification = {
        title: 'Test Title',
        message: 'Test message',
      };

      window.Notification.permission = 'denied';
      const NotificationSpy = vi.spyOn(window, 'Notification');

      NotificationService.showBrowserNotification(notification);

      expect(NotificationSpy).not.toHaveBeenCalled();
    });

    test('requests notification permission', async () => {
      window.Notification.permission = 'default';
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {});

      await NotificationService.requestNotificationPermission();

      expect(window.Notification.requestPermission).toHaveBeenCalled();
      
      // Wait for the promise to resolve
      await new Promise(resolve => setTimeout(resolve, 10));
      
      expect(consoleSpy).toHaveBeenCalledWith('🔔 Notification permission:', 'granted');
      
      consoleSpy.mockRestore();
    });

    test('handles case when Notification API is not available', () => {
      const originalNotification = window.Notification;
      delete window.Notification;

      const notification = {
        title: 'Test Title',
        message: 'Test message',
      };

      // Should not throw error
      expect(() => {
        NotificationService.showBrowserNotification(notification);
      }).not.toThrow();

      // Should not throw error
      expect(() => {
        NotificationService.requestNotificationPermission();
      }).not.toThrow();

      // Restore
      window.Notification = originalNotification;
    });
  });

  describe('Subscription Management', () => {
    test.skip('stores subscription for user', () => {
      const userId = 1;
      const onNotification = vi.fn();
      const mockSubscription = { unsubscribe: mockUnsubscribe };

      mockSubscribe.mockReturnValue(mockSubscription);

      const { Client } = require('@stomp/stompjs');
      Client.mockImplementation((config) => {
        setTimeout(() => {
          config.onConnect();
        }, 0);
        return mockStompClient;
      });

      NotificationService.connect(userId, onNotification);

      setTimeout(() => {
        expect(NotificationService.subscriptions.get(userId)).toBe(mockSubscription);
      }, 10);
    });

    test('clears subscriptions on disconnect', () => {
      const userId = 1;
      NotificationService.subscriptions.set(userId, { unsubscribe: mockUnsubscribe });
      NotificationService.stompClient = mockStompClient;

      NotificationService.disconnect();

      expect(NotificationService.subscriptions.size).toBe(0);
    });
  });

  describe('Error Handling', () => {
    test.skip('handles JSON parsing errors gracefully', () => {
      const userId = 1;
      const onNotification = vi.fn();
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      const { Client } = require('@stomp/stompjs');
      Client.mockImplementation((config) => {
        setTimeout(() => {
          config.onConnect();
        }, 0);
        return mockStompClient;
      });

      NotificationService.connect(userId, onNotification);

      setTimeout(() => {
        const subscribeCallback = mockSubscribe.mock.calls[0][1];
        
        // Simulate malformed JSON
        const mockMessage = { body: 'invalid json' };
        
        expect(() => {
          subscribeCallback(mockMessage);
        }).not.toThrow();

        expect(onNotification).not.toHaveBeenCalled();
      }, 10);

      consoleSpy.mockRestore();
    });
  });
});
