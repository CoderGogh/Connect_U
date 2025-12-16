document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('join-form');
    const errorBox = document.getElementById('error-box');
    const joinBtn = document.querySelector('.btn-join');

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const nickname = document.getElementById('nickname').value.trim();
        const description = document.getElementById('description').value.trim();

        errorBox.style.display = 'none';
        errorBox.textContent = '';

        if (!email || !password || !nickname) {
            errorBox.textContent = '이메일, 비밀번호, 닉네임은 필수 입력 항목입니다.';
            errorBox.style.display = 'block';
            return;
        }

        const originalBtnText = joinBtn.textContent;
        joinBtn.textContent = '회원가입 중...';
        joinBtn.disabled = true;

        try {
            const response = await window.cu.apiFetch('/api/auth/join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password, nickname, description })
            });

            if (response.ok) {
                await response.json();
                alert('회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.');
                window.location.href = '/login';
            } else {
                const errorData = await response.json().catch(() => ({}));
                errorBox.textContent = errorData.message || '회원가입에 실패했습니다. 입력 정보를 다시 확인해주세요.';
                errorBox.style.display = 'block';
            }
        } catch (error) {
            errorBox.textContent = error.message || '서버와 통신 중 오류가 발생했습니다.';
            errorBox.style.display = 'block';
        } finally {
            joinBtn.textContent = originalBtnText;
            joinBtn.disabled = false;
        }
    });
});
