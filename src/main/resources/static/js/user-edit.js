document.addEventListener('DOMContentLoaded', () => {
    const nicknameInput = document.getElementById('nickname');
    const passwordInput = document.getElementById('password');
    const descInput = document.getElementById('description');
    const profileInput = document.getElementById('profileImage');
    const nicknameLabel = document.getElementById('edit-nickname-label');
    const emailLabel = document.getElementById('edit-email-label');
    const errorBox = document.getElementById('edit-error');
    const form = document.getElementById('edit-form');

    let currentUserId = null;

    function showError(message) {
        if (!errorBox) return;
        errorBox.textContent = message;
        errorBox.style.display = 'block';
    }

    function hideError() {
        if (!errorBox) return;
        errorBox.style.display = 'none';
        errorBox.textContent = '';
    }

    async function loadMyInfo() {
        try {
            const res = await window.cu.apiFetch('/api/users/my-info');
            if (!res.ok) throw new Error('내 정보를 불러올 수 없습니다.');
            const data = await res.json();
            currentUserId = data.usersId;
            nicknameInput.value = data.nickname || '';
            descInput.value = data.description || '';
            nicknameLabel.textContent = data.nickname || '닉네임';
            emailLabel.textContent = data.email || '';
        } catch (err) {
            showError(err.message || '내 정보를 불러오는 중 오류가 발생했습니다.');
        }
    }

    async function uploadProfileImage() {
        const files = profileInput?.files;
        if (!files || !files.length || !currentUserId) return;
        const check = window.cu.validateImages(files, 1, 5 * 1024 * 1024);
        if (!check.valid) {
            throw new Error(check.errors.join('\n'));
        }
        const formData = new FormData();
        formData.append('file', files[0]);
        formData.append('usersId', currentUserId);
        const res = await window.cu.apiFetch('/api/users/images', {
            method: 'POST',
            body: formData,
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || '프로필 이미지 업로드에 실패했습니다.');
        }
    }

    async function submitForm(e) {
        e.preventDefault();
        hideError();
        if (!currentUserId) {
            showError('사용자 정보를 불러오지 못했습니다.');
            return;
        }
        const nickname = nicknameInput.value.trim();
        const password = passwordInput.value.trim();
        const description = descInput.value.trim();
        if (!nickname) {
            showError('닉네임은 필수입니다.');
            return;
        }
        const body = { nickname, description };
        if (password) body.password = password;

        try {
            const query = `?usersId=${encodeURIComponent(currentUserId)}`;
            const res = await window.cu.apiFetch(`/api/users${query}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body),
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || '프로필 수정에 실패했습니다.');
            }
            await uploadProfileImage();
            alert('프로필이 수정되었습니다. 다시 로그인 후 이용해주세요.');
            try {
                await window.cu.apiFetch('/logout', { method: 'POST' });
            } catch (err) {
                // 실패하더라도 로그인 화면으로 이동
            }
            window.location.href = '/login';
        } catch (err) {
            showError(err.message || '프로필 수정 중 오류가 발생했습니다.');
        }
    }

    if (form) {
        form.addEventListener('submit', submitForm);
    }

    loadMyInfo();
});
