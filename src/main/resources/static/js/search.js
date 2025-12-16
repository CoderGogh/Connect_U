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
    const commentState = {};
    const COMMENT_PAGE_SIZE = 10;

    const state = {
        tab: 'posts',
        keyword: (window.searchInitialKeyword || '').trim(),
        pageSize: 10,
        posts: { page: 0, total: 0 },
        users: { page: 0, total: 0 },
        currentUserId: null,
    };

    function escapeHtml(str) {
        const div = document.createElement('div');
        div.textContent = str ?? '';
        return div.innerHTML;
    }

    function formatContent(str) {
        return escapeHtml(str || '').replace(/\n/g, '<br>');
    }

    function getCommentState(postId) {
        if (!commentState[postId]) {
            commentState[postId] = { page: 0, total: 0, loaded: false };
        }
        return commentState[postId];
    }

    function renderCommentPagination(postId) {
        const module = postList?.querySelector(`.comment-module[data-id="${postId}"]`);
        if (!module) return;
        const pagination = module.querySelector('.comment-pagination');
        if (!pagination) return;
        const st = getCommentState(postId);
        const totalPages = Math.ceil((st.total || 0) / COMMENT_PAGE_SIZE);
        pagination.innerHTML = '';
        if (totalPages <= 1) return;
        for (let i = 0; i < totalPages; i++) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'comment-page-btn';
            btn.dataset.page = i.toString();
            btn.dataset.postId = postId;
            btn.textContent = (i + 1).toString();
            btn.disabled = i === st.page;
            pagination.appendChild(btn);
        }
    }

    function createCommentElement(comment, depth, postId) {
        const el = document.createElement('div');
        el.className = 'comment-item';
        el.dataset.commentId = comment.id || '';
        el.style.paddingLeft = `${depth * 16}px`;
        const isDeleted = comment.isDeleted === true;
        const liked = comment.isLiked === true;
        const heart = liked ? '❤️' : '🤍';
        const likeCount = typeof comment.likeCount === 'number' ? comment.likeCount : 0;
        const created = comment.createdAt ? new Date(comment.createdAt).toLocaleString() : '';
        const authorName = !isDeleted && comment.username ? comment.username : '익명';
        const canEditOrDelete =
            !isDeleted &&
            state.currentUserId &&
            comment.userId &&
            parseInt(state.currentUserId, 10) === parseInt(comment.userId, 10);

        el.innerHTML = `
            <div class="comment-head">
                <span class="comment-author">${escapeHtml(authorName)}</span>
                <span class="comment-date">${created}</span>
            </div>
            <div class="comment-body ${isDeleted ? 'comment-deleted' : ''}">${formatContent(
                comment.content || ''
            )}</div>
            <div class="comment-actions">
                <button class="comment-like-btn" data-comment-id="${comment.id || ''}" data-liked="${liked}" type="button" ${
            isDeleted ? 'disabled' : ''
        }>${heart} ${likeCount}</button>
                ${
                    !isDeleted
                        ? `<button class="comment-reply-btn" data-comment-id="${comment.id || ''}" data-post-id="${postId}" type="button">💬 대댓글</button>`
                        : ''
                }
            </div>
            <div class="reply-form" data-comment-id="${comment.id || ''}" style="display:none;">
                <textarea placeholder="대댓글을 입력하세요" data-parent-id="${comment.id || ''}"></textarea>
                <div class="comment-form-actions">
                    <button class="cu-btn reply-submit-btn" data-post-id="${postId}" data-parent-id="${comment.id || ''}" type="button">등록</button>
                </div>
            </div>
            ${
                canEditOrDelete
                    ? `<div class="comment-owner-actions">
                        <button class="cu-btn secondary comment-edit-btn" data-comment-id="${comment.id || ''}" data-post-id="${postId}" type="button">수정</button>
                        <button class="cu-btn danger comment-delete-btn" data-comment-id="${comment.id || ''}" data-post-id="${postId}" type="button">삭제</button>
                    </div>
                    <div class="edit-form" data-comment-id="${comment.id || ''}" style="display:none;">
                        <textarea class="edit-textarea" data-comment-id="${comment.id || ''}" placeholder="댓글을 수정하세요"></textarea>
                        <div class="comment-form-actions">
                            <button class="cu-btn reply-submit-btn comment-update-btn" data-post-id="${postId}" data-comment-id="${comment.id || ''}" type="button">저장</button>
                        </div>
                    </div>`
                    : ''
            }
        `;
        el.dataset.content = comment.content || '';
        const editTextarea = el.querySelector('.edit-textarea');
        if (editTextarea) {
            editTextarea.value = comment.content || '';
        }

        const children = Array.isArray(comment.children) ? comment.children : [];
        if (children.length > 0) {
            const childBox = document.createElement('div');
            childBox.className = 'comment-children';
            children.forEach((child) => {
                childBox.appendChild(createCommentElement(child, depth + 1, postId));
            });
            el.appendChild(childBox);
        }
        return el;
    }

    function renderComments(postId, comments) {
        const module = postList?.querySelector(`.comment-module[data-id="${postId}"]`);
        if (!module) return;
        const list = module.querySelector('.comment-list');
        if (!list) return;
        list.innerHTML = '';
        if (!comments || comments.length === 0) {
            list.innerHTML = '<div class="comment-empty">첫 댓글을 남겨보세요.</div>';
            return;
        }
        const frag = document.createDocumentFragment();
        comments.forEach((c) => frag.appendChild(createCommentElement(c, 0, postId)));
        list.appendChild(frag);
    }

    async function loadComments(postId, page = 0) {
        const st = getCommentState(postId);
        const module = postList?.querySelector(`.comment-module[data-id="${postId}"]`);
        if (!module) return;
        const list = module.querySelector('.comment-list');
        if (list) {
            list.innerHTML = '<div class="comment-empty">댓글을 불러오는 중입니다...</div>';
        }
        try {
            const query = window.cu.buildQuery({ page, size: COMMENT_PAGE_SIZE });
            const res = await window.cu.apiFetch(`/api/comments/${postId}?${query}`);
            if (!res.ok) throw new Error('댓글을 불러오지 못했습니다.');
            const data = await res.json();
            st.page = page;
            st.total = data.totalCount || 0;
            st.loaded = true;
            renderComments(postId, Array.isArray(data.content) ? data.content : []);
            renderCommentPagination(postId);
        } catch (err) {
            window.cu.showWarning(err.message || '댓글을 불러오지 못했습니다.');
        }
    }

    async function submitComment(postId, parentCommentId, textarea) {
        const content = textarea.value.trim();
        if (content.length === 0) {
            window.cu.showWarning('댓글을 입력해주세요.');
            return;
        }
        try {
            const body = {
                postId: Number(postId),
                parentCommentId: parentCommentId ? Number(parentCommentId) : null,
                content,
            };
            const res = await window.cu.apiFetch('/api/comments', {
                method: 'POST',
                body: JSON.stringify(body),
                redirectOn403: false,
            });
            if (res.status === 403 || res.status === 401) {
                alert('로그인 후 사용할 수 있습니다.');
                const loginPath = document.body.dataset.loginPath || '/login';
                window.location.href = loginPath;
                return;
            }
            if (!res.ok) throw new Error('댓글 작성에 실패했습니다.');
            textarea.value = '';
            await loadComments(postId, 0);
        } catch (err) {
            window.cu.showWarning(err.message || '댓글 작성 중 오류가 발생했습니다.');
        }
    }

    async function toggleCommentLike(button) {
        const commentId = button.dataset.commentId;
        if (!commentId) return;
        try {
            const res = await window.cu.apiFetch(`/api/comments/likes/${commentId}`, {
                method: 'POST',
                redirectOn403: false,
            });
            if (res.status === 403 || res.status === 401) {
                alert('로그인 후 사용할 수 있습니다.');
                const loginPath = document.body.dataset.loginPath || '/login';
                window.location.href = loginPath;
                return;
            }
            if (!res.ok) throw new Error('좋아요 처리에 실패했습니다.');
            const data = await res.json();
            const nextLiked = data.isLiked === true;
            const heart = nextLiked ? '❤️' : '🤍';
            const count = typeof data.likeCount === 'number' ? data.likeCount : 0;
            button.dataset.liked = nextLiked.toString();
            button.textContent = `${heart} ${Math.max(count, 0)}`;
        } catch (err) {
            window.cu.showWarning(err.message || '댓글 좋아요 처리 중 오류가 발생했습니다.');
        }
    }

    function toggleReplyForm(button) {
        const commentId = button.dataset.commentId;
        if (!commentId) return;
        const form = button.closest('.comment-item')?.querySelector('.reply-form');
        if (!form) return;
        const isHidden = form.style.display === 'none';
        form.style.display = isHidden ? 'block' : 'none';
        if (isHidden) {
            const textarea = form.querySelector('textarea');
            if (textarea) textarea.focus();
        }
    }

    async function deleteComment(button) {
        const commentId = button.dataset.commentId;
        const postId = button.dataset.postId;
        if (!commentId || !postId) return;
        const confirmed = window.confirm('댓글을 삭제하시겠습니까?');
        if (!confirmed) return;
        try {
            const res = await window.cu.apiFetch(`/api/comments/${commentId}`, { method: 'DELETE', redirectOn403: false });
            if (res.status === 403 || res.status === 401) {
                alert('로그인 후 사용할 수 있습니다.');
                const loginPath = document.body.dataset.loginPath || '/login';
                window.location.href = loginPath;
                return;
            }
            if (!res.ok) throw new Error('댓글 삭제에 실패했습니다.');
            await loadComments(postId, getCommentState(postId).page);
        } catch (err) {
            window.cu.showWarning(err.message || '댓글 삭제 중 오류가 발생했습니다.');
        }
    }

    function toggleEditForm(button) {
        const commentId = button.dataset.commentId;
        if (!commentId) return;
        const item = button.closest('.comment-item');
        const form = item?.querySelector('.edit-form');
        if (!form) return;
        const isHidden = form.style.display === 'none';
        form.style.display = isHidden ? 'block' : 'none';
        if (isHidden) {
            const textarea = form.querySelector('.edit-textarea');
            if (textarea) {
                const original = item?.dataset.content || '';
                textarea.value = original;
                textarea.focus();
            }
        }
    }

    async function submitCommentUpdate(button) {
        const commentId = button.dataset.commentId;
        const postId = button.dataset.postId;
        if (!commentId || !postId) return;
        const form = button.closest('.edit-form');
        const textarea = form?.querySelector('.edit-textarea');
        if (!textarea) return;
        const content = textarea.value.trim();
        if (content.length === 0) {
            window.cu.showWarning('댓글을 입력해주세요.');
            return;
        }
        try {
            const url = `/api/comments/${commentId}?content=${encodeURIComponent(content)}`;
            const res = await window.cu.apiFetch(url, { method: 'PATCH', redirectOn403: false });
            if (res.status === 403 || res.status === 401) {
                alert('로그인 후 사용할 수 있습니다.');
                const loginPath = document.body.dataset.loginPath || '/login';
                window.location.href = loginPath;
                return;
            }
            if (!res.ok) throw new Error('댓글 수정에 실패했습니다.');
            await loadComments(postId, getCommentState(postId).page);
        } catch (err) {
            window.cu.showWarning(err.message || '댓글 수정 중 오류가 발생했습니다.');
        }
    }

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
                        <div class="comment-form">
                            <textarea class="comment-input" data-post-id="${post.id || ''}" placeholder="댓글을 입력하세요"></textarea>
                            <div class="comment-form-actions">
                                <button class="cu-btn comment-submit-btn" data-post-id="${post.id || ''}" type="button">등록</button>
                            </div>
                        </div>
                        <div class="comment-list" data-post-id="${post.id || ''}"></div>
                        <div class="comment-pagination" data-post-id="${post.id || ''}"></div>
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
        const isHidden = module.style.display === 'none';
        module.style.display = isHidden ? 'block' : 'none';
        const st = getCommentState(postId);
        if (isHidden && !st.loaded) {
            loadComments(postId, 0);
        }
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
            } else if (target.classList.contains('comment-like-btn')) {
                toggleCommentLike(target);
            } else if (target.classList.contains('comment-reply-btn')) {
                toggleReplyForm(target);
            } else if (target.classList.contains('comment-submit-btn')) {
                const module = target.closest('.comment-module');
                const textarea = module?.querySelector('.comment-input');
                if (textarea) {
                    submitComment(target.dataset.postId, null, textarea);
                }
            } else if (target.classList.contains('reply-submit-btn')) {
                const form = target.closest('.reply-form');
                const textarea = form?.querySelector('textarea');
                if (textarea) {
                    submitComment(target.dataset.postId, target.dataset.parentId, textarea);
                }
            } else if (target.classList.contains('comment-page-btn')) {
                const postId = target.dataset.postId;
                const page = Number(target.dataset.page || 0);
                loadComments(postId, page);
            } else if (target.classList.contains('comment-delete-btn')) {
                deleteComment(target);
            } else if (target.classList.contains('comment-edit-btn')) {
                toggleEditForm(target);
            } else if (target.classList.contains('comment-update-btn')) {
                submitCommentUpdate(target);
            }
        });
    }

    init();
})();
