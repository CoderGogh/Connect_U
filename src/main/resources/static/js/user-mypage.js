document.addEventListener('DOMContentLoaded', () => {
    const nicknameEl = document.getElementById('my-nickname');
    const emailEl = document.getElementById('my-email');
    const descEl = document.getElementById('my-description');
    const deleteBtn = document.getElementById('btn-delete-account');

    async function loadMyInfo() {
        try {
            const res = await window.cu.apiFetch('/api/users/my-info');
            if (!res.ok) throw new Error('내 정보를 불러올 수 없습니다.');
            const data = await res.json();
            nicknameEl.textContent = data.nickname || '-';
            emailEl.textContent = data.email || '';
            descEl.textContent = data.description || '-';
            nicknameEl.dataset.userId = data.usersId;
        } catch (err) {
            nicknameEl.textContent = '알 수 없음';
            emailEl.textContent = '';
            descEl.textContent = '정보를 불러오지 못했습니다.';
        }
    }

    async function deleteAccount() {
        const userId = nicknameEl.dataset.userId;
        const confirmed = window.confirm('정말로 탈퇴하시겠습니까?');
        if (!confirmed) return;
        try {
            const query = userId ? `?usersId=${encodeURIComponent(userId)}` : '';
            const res = await window.cu.apiFetch(`/api/users${query}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('회원 탈퇴에 실패했습니다.');
            alert('탈퇴가 완료되었습니다. 메인으로 이동합니다.');
            window.location.href = '/';
        } catch (err) {
            alert(err.message || '회원 탈퇴 처리 중 오류가 발생했습니다.');
        }
    }

    if (deleteBtn) {
        deleteBtn.addEventListener('click', deleteAccount);
    }

    loadMyInfo();
});
