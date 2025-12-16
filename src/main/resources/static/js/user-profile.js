document.addEventListener('DOMContentLoaded', () => {
    const panel = document.querySelector('.user-panel');
    const userId = panel ? panel.dataset.userId : null;
    const nicknameEl = document.getElementById('profile-nickname');
    const emailEl = document.getElementById('profile-email');
    const descEl = document.getElementById('profile-description');

    if (!userId) {
        if (nicknameEl) nicknameEl.textContent = '사용자 식별자가 없습니다.';
        return;
    }

    async function loadProfile() {
        try {
            const res = await window.cu.apiFetch(`/api/users/id/${userId}`);
            if (!res.ok) throw new Error('사용자 정보를 불러올 수 없습니다.');
            const data = await res.json();
            nicknameEl.textContent = data.nickname || '-';
            emailEl.textContent = data.email || '';
            descEl.textContent = data.description || '-';
        } catch (err) {
            nicknameEl.textContent = '정보 없음';
            emailEl.textContent = '';
            descEl.textContent = err.message || '정보를 불러오지 못했습니다.';
        }
    }

    loadProfile();
});
