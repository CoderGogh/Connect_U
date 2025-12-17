document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('join-form');
    const errorBox = document.getElementById('error-box');
    const joinBtn = document.querySelector('.btn-join');
    const checkBtn = document.getElementById('btn-check-email');
    const emailHint = document.getElementById('email-check-hint');
    let emailChecked = false;
    let lastCheckedEmail = '';

    function showError(msg) {
        errorBox.style.display = 'block';
        errorBox.textContent = msg;
    }

    function clearError() {
        errorBox.style.display = 'none';
        errorBox.textContent = '';
    }

    function setEmailHint(msg, success = false) {
        if (!emailHint) return;
        emailHint.textContent = msg || '';
        emailHint.className = success ? 'input-hint success' : 'input-hint error';
    }

    async function checkEmail() {
        clearError();
        setEmailHint('');
        const email = document.getElementById('email').value.trim();
        if (!email) {
            showError('이메일을 입력해 주세요.');
            return;
        }
        try {
            checkBtn.disabled = true;
            const res = await window.cu.apiFetch(`/api/auth/check-email/${encodeURIComponent(email)}`, { method: 'GET' });
            if (!res.ok) {
                throw new Error('이메일 중복 확인에 실패했습니다.');
            }
            emailChecked = true;
            lastCheckedEmail = email;
            setEmailHint('사용 가능한 이메일입니다.', true);
        } catch (err) {
            emailChecked = false;
            lastCheckedEmail = '';
            setEmailHint(err.message || '사용할 수 없는 이메일입니다.', false);
        } finally {
            checkBtn.disabled = false;
        }
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const nickname = document.getElementById('nickname').value.trim();
        const description = document.getElementById('description').value.trim();

        clearError();

        if (!email || !password || !nickname) {
            showError('이메일, 비밀번호, 닉네임은 필수 입력 항목입니다.');
            return;
        }

        if (!emailChecked || lastCheckedEmail !== email) {
            showError('이메일 중복 검사를 완료해 주세요.');
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
                showError(errorData.message || '회원가입에 실패했습니다. 입력 정보를 다시 확인해주세요.');
            }
        } catch (error) {
            showError(error.message || '서버와 통신 중 오류가 발생했습니다.');
        } finally {
            joinBtn.textContent = originalBtnText;
            joinBtn.disabled = false;
        }
    });

    if (checkBtn) {
        checkBtn.addEventListener('click', checkEmail);
    }

    document.getElementById('email')?.addEventListener('input', () => {
        emailChecked = false;
        lastCheckedEmail = '';
        setEmailHint('');
    });
});
