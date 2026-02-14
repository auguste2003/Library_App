import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

export type DialogType = 'success' | 'error';

@Component({
    selector: 'app-dialog',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './dialog.html',
})
export class DialogComponent {
    isOpen = signal(false);
    title = signal('');
    message = signal('');
    type = signal<DialogType>('success');
    showCancel = signal(false);
    private onConfirmCallback: (() => void) | null = null;

    open(title: string, message: string, type: DialogType = 'success', onConfirm?: () => void) {
        this.title.set(title);
        this.message.set(message);
        this.type.set(type);
        this.showCancel.set(!!onConfirm);
        this.onConfirmCallback = onConfirm || null;
        this.isOpen.set(true);
    }

    close() {
        this.isOpen.set(false);
        this.onConfirmCallback = null;
    }

    confirm() {
        if (this.onConfirmCallback) {
            this.onConfirmCallback();
        }
        this.close();
    }
}
