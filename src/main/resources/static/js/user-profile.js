document.addEventListener('DOMContentLoaded', () => {
    const panel = document.querySelector('.user-panel');
    const userId = panel ? panel.dataset.userId : null;
    const nicknameEl = document.getElementById('profile-nickname');
    const emailEl = document.getElementById('profile-email');
    const descEl = document.getElementById('profile-description');
    const avatarEl = document.getElementById('profile-avatar');
    const followActions = document.getElementById('follow-actions');
    const followBtn = document.getElementById('follow-btn');
    const unfollowBtn = document.getElementById('unfollow-btn');
    const followersBtn = document.getElementById('btn-user-followers');
    const followingsBtn = document.getElementById('btn-user-followings');
    const modal = document.getElementById('follow-modal');
    const modalClose = document.getElementById('follow-modal-close');
    const modalTitle = document.getElementById('follow-modal-title');
    const listEl = document.getElementById('follow-list');
    const emptyEl = document.getElementById('follow-empty');
    const loadingEl = document.getElementById('follow-loading');

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

    const followState = {
        type: null, // 'followers' | 'followings'
        page: 0,
        size: 20,
        loading: false,
        done: false,
    };

    function resetModalState(type) {
        followState.type = type;
        followState.page = 0;
        followState.done = false;
        followState.loading = false;
        if (listEl) listEl.innerHTML = '';
        if (emptyEl) emptyEl.style.display = 'none';
        if (loadingEl) loadingEl.style.display = 'none';
        if (modalTitle) modalTitle.textContent = type === 'followers' ? '팔로워 목록' : '팔로잉 목록';
    }

    function setModalVisible(visible) {
        if (!modal) return;
        modal.setAttribute('aria-hidden', visible ? 'false' : 'true');
        modal.style.display = visible ? 'flex' : 'none';
    }

    function showLoading(active) {
        if (loadingEl) loadingEl.style.display = active ? 'block' : 'none';
    }

    function renderUsers(users) {
        users.forEach((user) => {
            const item = document.createElement('div');
            item.className = 'follow-item';
            const link = user.usersId != null ? `/users/${user.usersId}` : '#';
            const avatar = user.imageUrl
                ? `<img src="${user.imageUrl}" alt="프로필 이미지" class="avatar-img">`
                : `<div class="avatar placeholder"></div>`;
            item.innerHTML = `
                ${avatar}
                <div class="follow-info">
                    <div class="nickname"><a href="${link}">${user.nickname || '사용자'}</a></div>
                </div>
            `;
            listEl.appendChild(item);
        });
    }

    async function loadFollowPage() {
        if (followState.loading || followState.done || !followState.type) return;
        followState.loading = true;
        showLoading(true);
        try {
            const endpoint =
                followState.type === 'followers'
                    ? `/api/follow/followers/${userId}`
                    : `/api/follow/followings/${userId}`;
            const query = window.cu.buildQuery({ page: followState.page, size: followState.size });
            const res = await window.cu.apiFetch(`${endpoint}?${query}`);
            if (!res.ok) throw new Error('목록을 불러오지 못했습니다.');
            const data = await res.json();
            const content = Array.isArray(data.content) ? data.content : [];
            if (content.length === 0 && followState.page === 0) {
                if (emptyEl) emptyEl.style.display = 'block';
                followState.done = true;
                return;
            }
            if (content.length > 0) {
                renderUsers(content);
                followState.page += 1;
            }
            if (content.length < followState.size) {
                followState.done = true;
            }
        } catch (err) {
            window.cu.showWarning(err.message || '목록을 불러오지 못했습니다.');
        } finally {
            followState.loading = false;
            showLoading(false);
        }
    }

    function setupScrollLoading() {
        const body = modal?.querySelector('.cu-modal-body');
        if (!body) return;
        body.addEventListener('scroll', () => {
            if (body.scrollTop + body.clientHeight >= body.scrollHeight - 40) {
                loadFollowPage();
            }
        });
    }

    function openFollowModal(type) {
        resetModalState(type);
        setModalVisible(true);
        loadFollowPage();
    }

    function closeFollowModal() {
        setModalVisible(false);
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
            if (avatarEl) {
                if (data.imageUrl) {
                    avatarEl.innerHTML = `<img src="${data.imageUrl}" alt="프로필 이미지">`;
                } else {
                    avatarEl.innerHTML = '';
                }
            }
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
    if (followersBtn) followersBtn.addEventListener('click', () => openFollowModal('followers'));
    if (followingsBtn) followingsBtn.addEventListener('click', () => openFollowModal('followings'));
    if (modalClose) modalClose.addEventListener('click', closeFollowModal);
    modal?.querySelector('.cu-modal-overlay')?.addEventListener('click', closeFollowModal);
    setupScrollLoading();

    loadProfile();
});
