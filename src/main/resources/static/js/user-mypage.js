document.addEventListener('DOMContentLoaded', () => {
    const nicknameEl = document.getElementById('my-nickname');
    const emailEl = document.getElementById('my-email');
    const descEl = document.getElementById('my-description');
    const deleteBtn = document.getElementById('btn-delete-account');
    const followersBtn = document.getElementById('btn-my-followers');
    const followingsBtn = document.getElementById('btn-my-followings');
    const modal = document.getElementById('follow-modal');
    const modalClose = document.getElementById('follow-modal-close');
    const modalTitle = document.getElementById('follow-modal-title');
    const listEl = document.getElementById('follow-list');
    const emptyEl = document.getElementById('follow-empty');
    const loadingEl = document.getElementById('follow-loading');

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
        listEl.innerHTML = '';
        if (emptyEl) emptyEl.style.display = 'none';
        if (loadingEl) loadingEl.style.display = 'none';
        modalTitle.textContent = type === 'followers' ? '팔로워 목록' : '팔로잉 목록';
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
            item.innerHTML = `
                <div class="avatar placeholder"></div>
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
                    ? '/api/follow/my-followers'
                    : '/api/follow/my-followings';
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

    if (followersBtn) followersBtn.addEventListener('click', () => openFollowModal('followers'));
    if (followingsBtn) followingsBtn.addEventListener('click', () => openFollowModal('followings'));
    if (modalClose) modalClose.addEventListener('click', closeFollowModal);
    modal?.querySelector('.cu-modal-overlay')?.addEventListener('click', closeFollowModal);
    setupScrollLoading();

    loadMyInfo();
});
