// 公共工具函数
const BASE = '';

async function api(path, opts = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['X-Auth-Token'] = token;
  const res = await fetch(BASE + path, {
    ...opts, headers, body: opts.body ? JSON.stringify(opts.body) : undefined
  });
  const data = await res.json().catch(() => ({}));
  if (res.status === 401) { localStorage.removeItem('token'); localStorage.removeItem('user'); location.href = 'login.html'; return; }
  if (data.code !== 200) throw new Error(data.msg || '请求失败');
  return data.data;
}

function getUser() { try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; } }
function logout() { localStorage.removeItem('token'); localStorage.removeItem('user'); location.href = 'login.html'; }
function requireRole(roles) {
  const u = getUser(); if (!u) return location.href = 'login.html';
  if (roles && !roles.includes(u.role)) { alert('无权限访问'); location.href = 'index.html'; }
}

function toast(msg, isError = false) {
  const el = document.createElement('div');
  el.className = 'toast' + (isError ? ' error' : '');
  el.textContent = msg;
  document.body.appendChild(el);
  requestAnimationFrame(() => el.classList.add('show'));
  setTimeout(() => { el.classList.remove('show'); setTimeout(() => el.remove(), 300); }, 2400);
}

function escapeHtml(s) {
  if (s == null) return '';
  return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function fmtDate(d) { const x = new Date(d); return `${x.getFullYear()}-${String(x.getMonth() + 1).padStart(2, '0')}-${String(x.getDate()).padStart(2, '0')}`; }
function weekOf(d) { return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date(d).getDay()]; }
function periodText(p) { return p === 'AM' ? '上午' : p === 'PM' ? '下午' : '晚上'; }

function renderNav(active) {
  const u = getUser();
  const roleLinks = {
    PATIENT: [
      { href: 'index.html', label: '首页', icon: '🏥' },
      { href: 'ai.html', label: 'AI导诊', icon: '🤖' },
      { href: 'appointments.html', label: '在线挂号', icon: '📅' },
      { href: 'consult.html', label: '在线问诊', icon: '💬' },
      { href: 'records.html', label: '健康档案', icon: '📋' },
      { href: 'profile.html', label: '个人中心', icon: '👤' },
    ],
    DOCTOR: [
      { href: 'index.html', label: '首页', icon: '🏥' },
      { href: 'workbench.html', label: '工作台', icon: '👨‍⚕️' },
      { href: 'ai.html', label: 'AI助手', icon: '🤖' },
    ],
    ADMIN: [
      { href: 'index.html', label: '首页', icon: '🏥' },
      { href: 'admin.html', label: '管理后台', icon: '⚙️' },
    ]
  };
  const links = u ? (roleLinks[u.role] || roleLinks.PATIENT) : [
    { href: 'index.html', label: '首页', icon: '🏥' },
    { href: 'ai.html', label: 'AI导诊', icon: '🤖' },
  ];
  const nav = document.getElementById('nav');
  nav.innerHTML = `
    <a class="nav-brand" href="index.html">
      <div class="logo">✚</div>
      <span>智慧医院</span>
    </a>
    <div class="nav-links">
      ${links.map(l => `<a href="${l.href}" class="${active === l.href ? 'active' : ''}">${l.icon} ${l.label}</a>`).join('')}
    </div>
    ${u ? `
      <div class="nav-user">
        <div class="avatar">${u.name ? u.name.charAt(0) : '👤'}</div>
        <span class="username">${escapeHtml(u.name || '')}</span>
        <button class="btn btn-ghost btn-sm" onclick="logout()">退出</button>
      </div>
    ` : `<a class="btn btn-primary btn-sm" href="login.html">登录</a>`;
}
