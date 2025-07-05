import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import TripImage from '../TripImage';

describe('TripImage Component', () => {
  const defaultProps = {
    src: 'https://example.com/image.jpg',
    alt: 'Test Image',
    className: 'test-class',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Loading State', () => {
    test('displays loading placeholder initially', () => {
      render(<TripImage {...defaultProps} />);
      
      expect(screen.getByText('📷 Loading...')).toBeInTheDocument();
      expect(screen.getByText('📷 Loading...')).toHaveClass('loading-placeholder');
    });

    test('hides image while loading', () => {
      render(<TripImage {...defaultProps} />);
      
      const img = screen.getByAltText('Test Image');
      expect(img).toHaveStyle('display: none');
      expect(img).toHaveClass('loading');
    });
  });

  describe('Successful Image Load', () => {
    test('displays image after loading', async () => {
      render(<TripImage {...defaultProps} />);
      
      const img = screen.getByAltText('Test Image');
      
      // Simulate image load
      fireEvent.load(img);
      
      await waitFor(() => {
        expect(img).toHaveStyle('display: block');
        expect(img).not.toHaveClass('loading');
      });
      
      expect(screen.queryByText('📷 Loading...')).not.toBeInTheDocument();
    });

    test('applies correct classes and attributes', async () => {
      render(<TripImage {...defaultProps} />);
      
      const img = screen.getByAltText('Test Image');
      fireEvent.load(img);
      
      await waitFor(() => {
        expect(img).toHaveClass('trip-image');
        expect(img).toHaveAttribute('src', 'https://example.com/image.jpg');
        expect(img).toHaveAttribute('alt', 'Test Image');
      });
      
      const container = screen.getByRole('img').parentElement;
      expect(container).toHaveClass('trip-image-container', 'test-class');
    });
  });

  describe('Image Error Handling', () => {
    test('shows text fallback when image fails to load', async () => {
      render(<TripImage {...defaultProps} />);
      
      const img = screen.getByAltText('Test Image');
      
      // Simulate image error
      fireEvent.error(img);
      
      await waitFor(() => {
        expect(screen.getByText('Test Image')).toBeInTheDocument();
        expect(screen.getByText('🏛️')).toBeInTheDocument();
      });
      
      expect(screen.queryByRole('img')).not.toBeInTheDocument();
    });

    test('shows appropriate fallback icon for different types', () => {
      // Test by not providing src, which triggers fallback immediately
      const { rerender } = render(
        <TripImage alt="Test Image" className="test-class" fallbackType="hotel" />
      );
      
      expect(screen.getByText('🏨')).toBeInTheDocument();
      expect(screen.getByText('Test Image')).toBeInTheDocument();
      
      // Test restaurant fallback
      rerender(<TripImage alt="Test Image" className="test-class" fallbackType="restaurant" />);
      expect(screen.getByText('🍽️')).toBeInTheDocument();
      
      // Test spot fallback (default)
      rerender(<TripImage alt="Test Image" className="test-class" fallbackType="spot" />);
      expect(screen.getByText('🏛️')).toBeInTheDocument();
    });

    test('applies correct fallback classes', () => {
      render(<TripImage {...defaultProps} />);
      
      const img = screen.getByAltText('Test Image');
      fireEvent.error(img);
      
      const fallbackContainer = screen.getByText('Test Image').closest('.trip-image-text-fallback');
      expect(fallbackContainer).toHaveClass('trip-image-text-fallback', 'test-class');
      
      const fallbackContent = screen.getByText('Test Image').closest('.text-fallback-content');
      expect(fallbackContent).toHaveClass('text-fallback-content');
    });
  });

  describe('Edge Cases', () => {
    test('shows fallback immediately when no src provided', () => {
      render(<TripImage alt="No Source" className="no-src" />);
      
      expect(screen.getByText('No Source')).toBeInTheDocument();
      expect(screen.getByText('🏛️')).toBeInTheDocument();
      expect(screen.queryByRole('img')).not.toBeInTheDocument();
      expect(screen.queryByText('📷 Loading...')).not.toBeInTheDocument();
    });

    test('handles empty src string', () => {
      render(<TripImage src="" alt="Empty Source" />);
      
      expect(screen.getByText('Empty Source')).toBeInTheDocument();
      expect(screen.queryByRole('img')).not.toBeInTheDocument();
    });

    test('works without className prop', () => {
      render(<TripImage src="test.jpg" alt="No Class" />);
      
      const container = screen.getByAltText('No Class').parentElement;
      expect(container).toHaveClass('trip-image-container');
      expect(container).not.toHaveClass('undefined');
    });    test('works without alt text', async () => {
      render(<TripImage src="test.jpg" />);

      // Wait for image to load
      await waitFor(() => {
        const img = screen.getByRole('img', { hidden: true });
        expect(img).toHaveAttribute('src', 'test.jpg');
        // When no alt is provided, it might be null or empty string
        const altValue = img.getAttribute('alt');
        expect(altValue === null || altValue === '').toBe(true);
      });
    });
  });

  describe('Props Validation', () => {
    test('defaults fallbackType to spot when not provided', () => {
      render(<TripImage alt="Default Type" />);
      
      expect(screen.getByText('🏛️')).toBeInTheDocument();
    });

    test('handles invalid fallbackType gracefully', () => {
      render(<TripImage alt="Invalid Type" fallbackType="invalid" />);
      
      // Should not crash and should show the fallback text
      expect(screen.getByText('Invalid Type')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    test('maintains alt text for screen readers', () => {
      render(<TripImage {...defaultProps} />);
      
      const img = screen.getByAltText('Test Image');
      expect(img).toHaveAttribute('alt', 'Test Image');
    });

    test('fallback text is accessible', () => {
      render(<TripImage alt="Accessible Fallback" />);
      
      expect(screen.getByText('Accessible Fallback')).toBeInTheDocument();
    });
  });
});
