import React, { useEffect, useState } from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import NotificationCenter from '../NotificationCenter';
import AuthContext from '../../context/AuthContext';

// Use jest.mock() for problematic modules
jest.mock('../../services/notificationService', () => ({
  connect: jest.fn(),
  disconnect: jest.fn()
}));

jest.mock('../../api', () => ({
  get: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

// Mock AuthContext
const mockCurrentUser = {
  id: '123',
  name: 'Test User'
};

const mockAuthContext = {
  currentUser: mockCurrentUser
};

describe('NotificationCenter', () => {
  beforeEach(() => {
    // Reset all mocks before each test
    jest.clearAllMocks();
    
    // Mock the API responses
    const api = require('../../api');
    api.get.mockResolvedValue({
      data: [
        {
          id: '1',
          message: 'Test notification',
          type: 'WEATHER_ALERT',
          isRead: false,
          createdAt: new Date().toISOString()
        }
      ]
    });
  });

  it('renders notification bell and shows unread count', async () => {
    render(
      <AuthContext.Provider value={mockAuthContext}>
        <NotificationCenter />
      </AuthContext.Provider>
    );

    // Check if notification bell is rendered
    expect(screen.getByRole('button', { name: '' })).toBeInTheDocument();

    // Wait for notifications to load
    await waitFor(() => {
      // Check if unread count badge is shown
      const badge = screen.getByText('1');
      expect(badge).toBeInTheDocument();
    });
  });

  it('opens notification dropdown when bell is clicked', async () => {
    render(
      <AuthContext.Provider value={mockAuthContext}>
        <NotificationCenter />
      </AuthContext.Provider>
    );

    // Click the notification bell
    const bell = screen.getByRole('button', { name: '' });
    fireEvent.click(bell);

    // Wait for and verify dropdown content
    await waitFor(() => {
      expect(screen.getByText('Notifications')).toBeInTheDocument();
      expect(screen.getByText('Test notification')).toBeInTheDocument();
    });
  });
}); 