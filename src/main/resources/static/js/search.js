(function () {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const panels = {
        posts: document.getElementById('tab-posts'),
        users: document.getElementById('tab-users'),
    };
    const keywordInput = document.getElementById('keyword');
    const postList = document.getElementById('post-results');
    const userList = document.getElementById('user-results');
    const postEmpty = document.getElementById('post-empty');
    const userEmpty = document.getElementById('user-empty');
    const postPagination = document.getElementById('post-pagination');
    const userPagination = document.getElementById('user-pagination');

    const state = {
        tab: 'posts',
        keyword: (window.searchInitialKeyword || '').trim(),
        pageSize: 10,
        posts: { page: 0, total: 0 },
        users: { page: 0, total: 0 },
        currentUserId: null,
    };

    function switchTab(tab) {
        state.tab = tab;
        tabButtons.forEach((btn) => btn.classList.toggle('active', btn.dataset.tab === tab));
        Object.entries(panels).forEach(([key, panel]) => {
            if (panel) panel.classList.toggle('is-hidden', key !== tab);
        });
    }

    function validateKeywordOrWarn() {
        const value = keywordInput.value;
        if (!window.cu.validateKeyword(value)) {
            window.cu.showWarning('검색어는 공백을 제외하고 2자 이상 입력해주세요.');
            return false;
        }
        state.keyword = value.trim();
        return true;
    }

    function renderPosts(data) {
        const content = Array.isArray(data.content) ? data.content : [];
        postList.innerHTML = '';
        if (content.length === 0) {
            postEmpty.style.display = 'block';
        } else {
            postEmpty.style.display = 'none';
            content.forEach((post) => {
                const card = document.createElement('article');
                card.className = 'post-card';
                const created = post.createdAt ? new Date(post.createdAt).toLocaleString() : '';
                const updated = post.updatedAt ? new Date(post.updatedAt).toLocaleString() : '';
                const images = Array.isArray(post.images) ? post.images : [];
                const liked = post.isLiked === true;
                const likeCount = typeof post.likeCount === 'number' ? post.likeCount : 0;
                const heart = liked ? '❤️' : '🤍';
                const authorLink = post.authorId ? `<a href="/users/${post.authorId}">${post.authorUsername || '익명'}</a>` : (post.authorUsername || '익명');
                const isOwner = state.currentUserId && post.authorId && parseInt(state.currentUserId, 10) === parseInt(post.authorId, 10);
                const imgSection = images.length
                    ? `<div class="post-images">${images.map((img) => `<img src="${img.imageUrl || ''}" alt="post image" style="max-width:100%;border-radius:8px;">`).join('')}</div>`
                    : '';
                card.innerHTML = `
                    <div class="post-meta">
                        <span>${authorLink}</span>
                        <span>${created}</span>
                    </div>
                    <h3 class="post-title">${post.title || ''}</h3>
                    <div class="post-content">${(post.content || '').replace(/\n/g, '<br>')}</div>
                    ${imgSection}
                    <div class="post-actions">
                        <button class="like-btn" data-id="${post.id || ''}" data-liked="${liked}" type="button">${heart} ${likeCount}</button>
                        <button class="comment-btn" data-id="${post.id || ''}" type="button">💬 댓글</button>
                        <span class="post-meta">업데이트: ${updated}</span>
                    </div>
                    <div class="comment-module" data-id="${post.id || ''}" style="display:none;">
                        <div class="comment-header">댓글 영역 (추후 API 연동)</div>
                    </div>
                    ${isOwner ? `
                    <div class="post-owner-actions">
                        <a class="cu-btn secondary" href="/posts/${post.id || ''}/edit">수정</a>
                        <button class="cu-btn danger delete-post-btn" data-id="${post.id || ''}" type="button">삭제</button>
                    </div>` : ''}
                `;
                postList.appendChild(card);
            });
        }
        state.posts.total = data.totalCount || 0;
        buildPagination(postPagination, state.posts.page, state.posts.total, onPostPageChange);
    }

    function renderUsers(data) {
        const content = Array.isArray(data.content) ? data.content : [];
        userList.innerHTML = '';
        if (content.length === 0) {
            userEmpty.style.display = 'block';
        } else {
            userEmpty.style.display = 'none';
            content.forEach((user) => {
                const card = document.createElement('div');
                card.className = 'user-card';
                const profileLink = user.usersId != null ? `/users/${user.usersId}` : '#';
                card.innerHTML = `
                    <div class="avatar"></div>
                    <div>
                        <div class="nickname"><a href="${profileLink}">${user.nickname || '사용자'}</a></div>
                        <div class="user-meta">ID: ${user.usersId ?? ''}</div>
                    </div>
                `;
                userList.appendChild(card);
            });
        }
        state.users.total = data.totalCount || 0;
        buildPagination(userPagination, state.users.page, state.users.total, onUserPageChange);
    }

    function buildPagination(container, page, totalCount, onChange) {
        if (!container) return;
        container.innerHTML = '';
        const size = state.pageSize;
        const totalPages = Math.ceil((totalCount || 0) / size);
        if (totalPages <= 1) return;
        const createBtn = (p, label) => {
            const btn = document.createElement('button');
            btn.textContent = label;
            btn.disabled = p === page;
            btn.classList.toggle('active', p === page);
            btn.addEventListener('click', () => onChange(p));
            return btn;
        };
        for (let i = 0; i < totalPages; i++) {
            container.appendChild(createBtn(i, (i + 1).toString()));
        }
    }

    async function fetchPosts(page = 0) {
        const query = window.cu.buildQuery({ keyword: state.keyword, page, size: state.pageSize });
        const res = await window.cu.apiFetch(`/api/search/post?${query}`);
        return res.json();
    }

    async function fetchUsers(page = 0) {
        const query = window.cu.buildQuery({ keyword: state.keyword, page, size: state.pageSize });
        const res = await window.cu.apiFetch(`/api/search/users?${query}`);
        return res.json();
    }

    async function onPostPageChange(p) {
        state.posts.page = p;
        const data = await fetchPosts(p);
        renderPosts(data);
    }

    async function onUserPageChange(p) {
        state.users.page = p;
        const data = await fetchUsers(p);
        renderUsers(data);
    }

    function onTabClick(e) {
        const tab = e.currentTarget.dataset.tab;
        switchTab(tab);
        if (tab === 'posts' && postList.children.length === 0) {
            onPostPageChange(0);
        }
        if (tab === 'users' && userList.children.length === 0) {
            onUserPageChange(0);
        }
    }

    async function init() {
        try {
            const res = await window.cu.apiFetch('/api/users/my-info');
            if (res.ok) {
                const data = await res.json();
                state.currentUserId = data.usersId;
                window.searchCurrentUserId = data.usersId;
            }
        } catch (e) {
            state.currentUserId = null;
            window.searchCurrentUserId = null;
        }

        tabButtons.forEach((btn) => btn.addEventListener('click', onTabClick));
        document.getElementById('search-form')?.addEventListener('submit', (e) => {
            if (!validateKeywordOrWarn()) {
                e.preventDefault();
            }
        });
        if (state.keyword && window.cu.validateKeyword(state.keyword)) {
            onPostPageChange(0);
        }
    }

    async function toggleLike(button) {
        const postId = button.dataset.id;
        if (!postId) return;
        const liked = button.dataset.liked === 'true';
        const currentCount = parseInt(button.textContent.replace(/[^0-9]/g, ''), 10) || 0;
        try {
            const method = liked ? 'DELETE' : 'POST';
            const res = await window.cu.apiFetch(`/api/posts/likes/${postId}`, { method, redirectOn403: false });
            if (res.status === 403 || res.status === 401) {
                alert('로그인 후 사용할 수 있습니다.');
                const loginPath = document.body.dataset.loginPath || '/login';
                window.location.href = loginPath;
                return;
            }
            if (!res.ok) throw new Error('요청에 실패했습니다.');
            const nextCount = liked ? currentCount - 1 : currentCount + 1;
            const nextLiked = !liked;
            button.dataset.liked = nextLiked.toString();
            const heart = nextLiked ? '❤️' : '🤍';
            button.textContent = `${heart} ${Math.max(nextCount, 0)}`;
        } catch (err) {
            alert(err.message || '좋아요 처리 중 오류가 발생했습니다.');
        }
    }

    function toggleComment(button) {
        const postId = button.dataset.id;
        if (!postId) return;
        const module = postList.querySelector(`.comment-module[data-id="${postId}"]`);
        if (!module) return;
        module.style.display = module.style.display === 'none' ? 'block' : 'none';
    }

    async function deletePost(button) {
        const postId = button.dataset.id;
        if (!postId) return;
        const confirmed = window.confirm('게시글을 삭제하시겠습니까?');
        if (!confirmed) return;
        try {
            const res = await window.cu.apiFetch(`/api/posts/${postId}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('삭제에 실패했습니다.');
            alert('게시글이 삭제되었습니다.');
            button.closest('.post-card')?.remove();
        } catch (err) {
            alert(err.message || '게시글 삭제 중 오류가 발생했습니다.');
        }
    }

    if (postList) {
        postList.addEventListener('click', (e) => {
            const target = e.target.closest('button');
            if (!target) return;
            if (target.classList.contains('like-btn')) {
                toggleLike(target);
            } else if (target.classList.contains('comment-btn')) {
                toggleComment(target);
            } else if (target.classList.contains('delete-post-btn')) {
                deletePost(target);
            }
        });
    }

    init();
})();
