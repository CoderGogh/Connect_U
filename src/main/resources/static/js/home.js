document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.getElementById('search-form');
    const keywordInput = document.getElementById('keyword');
    const sortButtons = document.querySelectorAll('.sort-btn');
    const postEmpty = document.getElementById('post-empty');
    const postList = document.getElementById('post-list');
    const loader = document.getElementById('feed-loader');
    const sentinel = document.getElementById('feed-sentinel');

    const state = {
        sort: 'latest',
        followingPage: 0,
        generalPage: 0,
        loading: false,
        followingDone: false,
        generalDone: false,
        pageSize: 10,
    };

    function setLoader(active) {
        if (!loader) return;
        loader.classList.toggle('active', active);
    }

    function renderPostCard(post) {
        const card = document.createElement('article');
        card.className = 'post-card';
        const created = post.createdAt ? new Date(post.createdAt).toLocaleString() : '';
        const updated = post.updatedAt ? new Date(post.updatedAt).toLocaleString() : '';
        const images = Array.isArray(post.images) ? post.images : [];
        const liked = Boolean(post.isLiked);
        const likeCount = typeof post.likeCount === 'number' ? post.likeCount : 0;
        const heart = liked ? '❤️' : '🤍';

        const imgSection = images.length
            ? `<div class="post-images">${images
                  .map((img) => `<img src="${img.imageUrl || ''}" alt="post image" style="max-width:100%;border-radius:8px;">`)
                  .join('')}</div>`
            : '';

        card.innerHTML = `
            <div class="post-meta">
                <span>${post.authorUsername || '익명'}</span>
                <span>${created}</span>
            </div>
            <h3 class="post-title">${post.title || ''}</h3>
            <div class="post-content">${(post.content || '').replace(/\\n/g, '<br>')}</div>
            ${imgSection}
            <div class="post-actions">
                <button class="like-btn" data-id="${post.id || ''}" data-liked="${liked}" type="button">${heart} ${likeCount}</button>
                <button class="comment-btn" data-id="${post.id || ''}" type="button">💬 댓글</button>
                <span class="post-meta">업데이트: ${updated}</span>
            </div>
            <div class="comment-module" data-id="${post.id || ''}" style="display:none;">
                <div class="comment-header">댓글 영역 (추후 API 연동)</div>
            </div>
        `;
        postList.appendChild(card);
    }

    function updateEmptyState(show, message) {
        if (!postEmpty) return;
        postEmpty.style.display = show ? 'block' : 'none';
        if (message) postEmpty.textContent = message;
    }

    async function fetchPosts(url) {
        const res = await window.cu.apiFetch(url);
        return res.json();
    }

    async function loadNext() {
        if (state.loading || (state.followingDone && state.generalDone)) return;
        state.loading = true;
        setLoader(true);

        try {
            let content = [];
            if (!state.followingDone) {
                const endpoint =
                    state.sort === 'likes'
                        ? '/api/posts/following-likes'
                        : '/api/posts/following-latest';
                const query = window.cu.buildQuery({ page: state.followingPage, size: state.pageSize });
                const data = await fetchPosts(`${endpoint}?${query}`);
                content = Array.isArray(data.content) ? data.content : [];
                if (content.length === 0) {
                    state.followingDone = true;
                } else {
                    state.followingPage += 1;
                }
            }

            if (content.length === 0 && !state.generalDone) {
                const endpoint = state.sort === 'likes' ? '/api/posts/likes' : '/api/posts/latest';
                const query = window.cu.buildQuery({ page: state.generalPage, size: state.pageSize });
                const data = await fetchPosts(`${endpoint}?${query}`);
                content = Array.isArray(data.content) ? data.content : [];
                if (content.length === 0) {
                    state.generalDone = true;
                } else {
                    state.generalPage += 1;
                }
            }

            if (content.length > 0) {
                updateEmptyState(false);
                content.forEach(renderPostCard);
            } else if (state.followingDone && state.generalDone) {
                updateEmptyState(true, '더 이상 게시글이 없습니다.');
            }
        } catch (err) {
            console.error(err);
            window.cu.showWarning(err.message || '게시글을 불러오지 못했습니다.');
        } finally {
            state.loading = false;
            setLoader(false);
        }
    }

    async function toggleLike(button) {
        const postId = button.dataset.id;
        if (!postId) return;
        const liked = button.dataset.liked === 'true';
        const currentCount = parseInt(button.textContent.replace(/[^0-9]/g, ''), 10) || 0;
        try {
            const method = liked ? 'DELETE' : 'POST';
            const endpoint = `/api/posts/likes/${postId}`;
            const res = await window.cu.apiFetch(endpoint, { method });
            if (!res.ok) throw new Error('요청에 실패했습니다.');
            const nextCount = liked ? currentCount - 1 : currentCount + 1;
            const nextLiked = !liked;
            button.dataset.liked = nextLiked.toString();
            const heart = nextLiked ? '❤️' : '🤍';
            button.textContent = `${heart} ${Math.max(nextCount, 0)}`;
        } catch (err) {
            window.cu.showWarning(err.message || '좋아요 처리 중 오류가 발생했습니다.');
        }
    }

    function toggleComment(button) {
        const postId = button.dataset.id;
        if (!postId) return;
        const module = postList.querySelector(`.comment-module[data-id=\"${postId}\"]`);
        if (!module) return;
        const isHidden = module.style.display === 'none';
        module.style.display = isHidden ? 'block' : 'none';
    }

    function resetFeed() {
        state.followingPage = 0;
        state.generalPage = 0;
        state.followingDone = false;
        state.generalDone = false;
        if (postList) postList.innerHTML = '';
        updateEmptyState(true, '게시글을 불러오는 중입니다.');
        loadNext();
    }

    if (searchForm && keywordInput) {
        searchForm.addEventListener('submit', (e) => {
            if (!window.cu.validateKeyword(keywordInput.value)) {
                e.preventDefault();
                window.cu.showWarning('검색어는 공백을 제외하고 2자 이상 입력해주세요.');
            }
        });
    }

    sortButtons.forEach((btn) => {
        btn.addEventListener('click', () => {
            sortButtons.forEach((b) => b.classList.remove('active'));
            btn.classList.add('active');
            state.sort = btn.dataset.sort || 'latest';
            resetFeed();
        });
    });

    if (postEmpty) {
        postEmpty.textContent = '게시글 목록을 불러오는 중입니다.';
    }

    if ('IntersectionObserver' in window && sentinel) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    loadNext();
                }
            });
        });
        observer.observe(sentinel);
    } else {
        // fallback: scroll listener
        window.addEventListener('scroll', () => {
            if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
                loadNext();
            }
        });
    }

    if (postList) {
        postList.addEventListener('click', (e) => {
            const target = e.target.closest('button');
            if (!target) return;
            if (target.classList.contains('like-btn')) {
                toggleLike(target);
            } else if (target.classList.contains('comment-btn')) {
                toggleComment(target);
            }
        });
    }

    resetFeed();
});
