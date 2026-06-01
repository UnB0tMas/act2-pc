document.addEventListener('DOMContentLoaded', () => {
    initSidebar();
    initActiveSidebarLink();
    initPagination();
    initConfirmButtons();
    initPasswordToggle();
    initAlerts();
});

function initSidebar() {
    const sidebar = document.getElementById('appSidebar');
    const openBtn = document.getElementById('sidebarOpenBtn');
    const backdrop = document.getElementById('sidebarBackdrop');

    if (!sidebar || !openBtn || !backdrop) {
        return;
    }

    const open = () => {
        sidebar.classList.remove('-translate-x-full');
        backdrop.classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
    };

    const close = () => {
        sidebar.classList.add('-translate-x-full');
        backdrop.classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
    };

    openBtn.addEventListener('click', open);
    backdrop.addEventListener('click', close);

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            close();
        }
    });
}

function initActiveSidebarLink() {
    const path = window.location.pathname;
    const links = document.querySelectorAll('.sidebar-link[data-path]');

    links.forEach((link) => {
        const target = link.getAttribute('data-path');

        if (target && (path === target || path.startsWith(target + '/'))) {
            link.classList.add('active');
        }
    });
}

function initPagination() {
    const links = document.querySelectorAll('.js-page-link[data-page]');

    links.forEach((link) => {
        link.addEventListener('click', (event) => {
            event.preventDefault();

            const page = link.getAttribute('data-page');
            if (page === null || page === '') {
                return;
            }

            const url = new URL(window.location.href);
            url.searchParams.set('page', page);

            if (!url.searchParams.has('size')) {
                url.searchParams.set('size', '10');
            }

            window.location.href = url.toString();
        });
    });
}

function initConfirmButtons() {
    const buttons = document.querySelectorAll('.js-confirm');

    buttons.forEach((button) => {
        button.addEventListener('click', (event) => {
            const message = button.getAttribute('data-confirm') || '¿Confirmas esta operación?';

            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });
}

function initPasswordToggle() {
    const buttons = document.querySelectorAll('.js-toggle-password');

    buttons.forEach((button) => {
        button.addEventListener('click', () => {
            const targetId = button.getAttribute('data-target');
            const input = document.getElementById(targetId);

            if (!input) {
                return;
            }

            const icon = button.querySelector('i');

            if (input.type === 'password') {
                input.type = 'text';
                icon?.classList.remove('fa-eye');
                icon?.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon?.classList.remove('fa-eye-slash');
                icon?.classList.add('fa-eye');
            }
        });
    });
}

function initAlerts() {
    const alerts = document.querySelectorAll('.app-alert');

    alerts.forEach((alert) => {
        setTimeout(() => {
            alert.style.transition = 'opacity 300ms ease, transform 300ms ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-6px)';

            setTimeout(() => {
                alert.remove();
            }, 320);
        }, 4500);
    });
}