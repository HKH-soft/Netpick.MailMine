import { render, screen, fireEvent } from '@testing-library/react';
import { Modal } from './index';
import { describe, it, expect, vi, beforeEach } from 'vitest';

describe('Modal component', () => {
  const mockOnClose = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns null when closed', () => {
    const { container } = render(
      <Modal isOpen={false} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    expect(container.firstChild).toBeNull();
  });

  it('renders children when open', () => {
    render(
      <Modal isOpen={true} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    expect(screen.getByText('Modal content')).toBeInTheDocument();
  });

  it('renders close button by default', () => {
    render(
      <Modal isOpen={true} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    const closeButton = screen.getByRole('button');
    expect(closeButton).toBeInTheDocument();
  });

  it('hides close button when showCloseButton is false', () => {
    render(
      <Modal isOpen={true} onCloseAction={mockOnClose} showCloseButton={false}>
        <div>Modal content</div>
      </Modal>
    );

    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', () => {
    render(
      <Modal isOpen={true} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    const closeButton = screen.getByRole('button');
    fireEvent.click(closeButton);

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('calls onClose when backdrop is clicked', () => {
    render(
      <Modal isOpen={true} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    const backdrop = document.querySelector('.fixed.inset-0');
    if (backdrop) {
      fireEvent.click(backdrop);
    }

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('calls onClose when Escape key is pressed', () => {
    render(
      <Modal isOpen={true} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    fireEvent.keyDown(document, { key: 'Escape' });

    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('does not call onClose when closed and Escape is pressed', () => {
    render(
      <Modal isOpen={false} onCloseAction={mockOnClose}>
        <div>Modal content</div>
      </Modal>
    );

    fireEvent.keyDown(document, { key: 'Escape' });

    expect(mockOnClose).not.toHaveBeenCalled();
  });

  it('applies fullscreen class correctly', () => {
    const { container } = render(
      <Modal isOpen={true} onCloseAction={mockOnClose} isFullscreen={true}>
        <div>Modal content</div>
      </Modal>
    );

    const modalContent = container.querySelector('.z-20');
    expect(modalContent?.classList.contains('w-full')).toBe(true);
    expect(modalContent?.classList.contains('h-full')).toBe(true);
  });

  it('applies custom className', () => {
    const { container } = render(
      <Modal isOpen={true} onCloseAction={mockOnClose} className="custom-class">
        <div>Modal content</div>
      </Modal>
    );

    const modalContent = container.querySelector('.z-20');
    expect(modalContent?.classList.contains('custom-class')).toBe(true);
  });
});