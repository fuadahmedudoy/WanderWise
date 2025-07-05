import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class NotificationService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.subscriptions = new Map();
    }

    connect(userId, onNotification) {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log('STOMP: ' + str),
            onConnect: () => {
                console.log('🔌 Connected to WebSocket');
                this.connected = true;
                
                // Subscribe to user-specific notifications using the correct path
                const subscription = this.stompClient.subscribe(
                    `/queue/notifications`, 
                    (message) => {
                        const notification = JSON.parse(message.body);
                        console.log('📢 Received notification:', notification);
                        onNotification(notification);
                    }
                );
                
                this.subscriptions.set(userId, subscription);
            },
            onDisconnect: () => {
                console.log('🔌 Disconnected from WebSocket');
                this.connected = false;
                this.subscriptions.clear();
            },
            onStompError: (frame) => {
                console.error('❌ STOMP error:', frame);
            }
        });

        this.stompClient.activate();
    }

    disconnect() {
        if (this.stompClient) {
            this.subscriptions.forEach(sub => sub.unsubscribe());
            this.subscriptions.clear();
            this.stompClient.deactivate();
            this.connected = false;
        }
    }

    // Send notification (for testing)
    sendTestNotification(userId, message) {
        if (this.connected && this.stompClient) {
            this.stompClient.publish({
                destination: '/app/notify',
                body: JSON.stringify({
                    userId: userId,
                    message: message,
                    type: 'WEATHER_ALERT'
                })
            });
        }
    }

    showBrowserNotification(notification) {
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(notification.title, {
                body: notification.message,
                icon: '/favicon.ico',
                badge: '/favicon.ico'
            });
        }
    }

    requestNotificationPermission() {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission().then(permission => {
                console.log('🔔 Notification permission:', permission);
            });
        }
    }
}

export default new NotificationService(); 