(function () {
    const panel = document.querySelector('.post-form-panel');
    if (!panel) return;

    const mode = panel.dataset.mode || 'create';
    const postId = panel.dataset.postId;
    const titleInput = document.getElementById('title');
    const contentInput = document.getElementById('content');
    const imagesInput = document.getElementById('images');
    const existingImages = document.getElementById('existing-images');
    const errorBox = document.getElementById('post-error');
    const form = document.getElementById('post-form');

    function showError(msg) {
        if (!errorBox) return;
        errorBox.textContent = msg;
        errorBox.style.display = 'block';
    }

    function hideError() {
        if (!errorBox) return;
        errorBox.style.display = 'none';
        errorBox.textContent = '';
    }

    function renderExistingImages(images) {
        if (!existingImages) return;
        existingImages.innerHTML = '';
        if (!images || !images.length) {
            existingImages.innerHTML = '<div class="post-hint">이미지가 없습니다.</div>';
            return;
        }
        images.forEach((img) => {
            const item = document.createElement('div');
            item.className = 'image-item';
            item.innerHTML = `
                <label style="display:flex; gap:10px; align-items:center;">
                    <input type="checkbox" class="delete-image" value="${img.seq}" />
                    <img src="${img.imageUrl || ''}" alt="image" />
                    <span>${img.imageKey || ''}</span>
                </label>
            `;
            existingImages.appendChild(item);
        });
    }

    async function fetchPost() {
        if (mode !== 'edit' || !postId) return;
        try {
            const res = await window.cu.apiFetch(`/api/posts/${postId}`);
            if (!res.ok) throw new Error('게시글 정보를 불러올 수 없습니다.');
            const data = await res.json();
            titleInput.value = data.title || '';
            contentInput.value = data.content || '';
            renderExistingImages(data.images || []);
        } catch (err) {
            showError(err.message || '게시글 정보를 불러오는 중 오류가 발생했습니다.');
        }
    }

    async function uploadImages(newPostId) {
        const files = imagesInput?.files;
        if (!files || !files.length) return;
        const check = window.cu.validateImages(files, 5, 5 * 1024 * 1024);
        if (!check.valid) {
            throw new Error(check.errors.join('\n'));
        }
        for (const file of check.accepted) {
            const formData = new FormData();
            formData.append('file', file);
            const res = await window.cu.apiFetch(`/api/posts/${newPostId}/images`, {
                method: 'POST',
                body: formData,
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || '이미지 업로드에 실패했습니다.');
            }
        }
    }

    function collectDeleteImageSeqs() {
        if (!existingImages) return [];
        return Array.from(existingImages.querySelectorAll('.delete-image:checked')).map((el) => el.value).filter(Boolean);
    }

    async function submitForm(e) {
        e.preventDefault();
        hideError();
        const title = titleInput.value.trim();
        const content = contentInput.value.trim();
        if (!title || !content) {
            showError('제목과 내용을 입력해주세요.');
            return;
        }

        const body = { title, content };

        try {
            if (mode === 'create') {
                const res = await window.cu.apiFetch('/api/posts', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body),
                });
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || '게시글 작성에 실패했습니다.');
                }
                const data = await res.json();
                const newId = data.id;
                await uploadImages(newId);
                alert('게시글이 작성되었습니다.');
                window.location.href = '/';
            } else {
                const deleteImageSeqs = collectDeleteImageSeqs();
                const query = deleteImageSeqs.length
                    ? `?deleteImageSeqs=${deleteImageSeqs.join('&deleteImageSeqs=')}`
                    : '';
                const res = await window.cu.apiFetch(`/api/posts/${postId}${query}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body),
                });
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || '게시글 수정에 실패했습니다.');
                }
                await uploadImages(postId);
                alert('게시글이 수정되었습니다.');
                window.location.href = '/';
            }
        } catch (err) {
            showError(err.message || '요청 처리 중 오류가 발생했습니다.');
        }
    }

    if (form) {
        form.addEventListener('submit', submitForm);
    }

    fetchPost();
})();
