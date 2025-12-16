document.addEventListener('DOMContentLoaded', () => {
    const panel = document.querySelector('.user-panel');
    const userId = panel ? panel.dataset.userId : null;
    const nicknameEl = document.getElementById('profile-nickname');
    const emailEl = document.getElementById('profile-email');
    const descEl = document.getElementById('profile-description');
    const followActions = document.getElementById('follow-actions');
    const followBtn = document.getElementById('follow-btn');
    const unfollowBtn = document.getElementById('unfollow-btn');

    if (!userId) {
        if (nicknameEl) nicknameEl.textContent = '사용자 식별자가 없습니다.';
        return;
    }

    function setFollowUi(state) {
        // state: true(팔로우 중), false(아님), null(표시 안 함)
        if (state === null) {
            if (followActions) followActions.style.display = 'none';
            return;
        }
        if (followActions) followActions.style.display = 'flex';
        if (state === true) {
            if (followBtn) followBtn.style.display = 'none';
            if (unfollowBtn) unfollowBtn.style.display = 'inline-flex';
        } else {
            if (followBtn) followBtn.style.display = 'inline-flex';
            if (unfollowBtn) unfollowBtn.style.display = 'none';
        }
    }

    async function toggleFollow(doFollow) {
        try {
            const method = doFollow ? 'POST' : 'DELETE';
            const res = await window.cu.apiFetch(`/api/follow/${userId}`, { method, redirectOn403: false });
            if (res.status === 401 || res.status === 403) {
                alert('로그인 후 사용할 수 있습니다.');
                window.location.href = '/login';
                return;
            }
            if (!res.ok) throw new Error('요청에 실패했습니다.');
            setFollowUi(doFollow);
        } catch (err) {
            window.cu.showWarning(err.message || '팔로우 처리 중 오류가 발생했습니다.');
        }
    }

    async function loadProfile() {
        try {
            const res = await window.cu.apiFetch(`/api/users/id/${userId}`);
            if (!res.ok) throw new Error('사용자 정보를 불러올 수 없습니다.');
            const data = await res.json();
            nicknameEl.textContent = data.nickname || '-';
            emailEl.textContent = data.email || '';
            descEl.textContent = data.description || '-';
            setFollowUi(data.isFollowing);
        } catch (err) {
            nicknameEl.textContent = '정보 없음';
            emailEl.textContent = '';
            descEl.textContent = err.message || '정보를 불러오지 못했습니다.';
        }
    }

    if (followBtn) {
        followBtn.addEventListener('click', () => toggleFollow(true));
    }
    if (unfollowBtn) {
        unfollowBtn.addEventListener('click', () => toggleFollow(false));
    }

    loadProfile();
});
