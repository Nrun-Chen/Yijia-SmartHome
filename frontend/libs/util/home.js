// utils/home.js
export function normalizeHome(h, fallbackName = '未命名家庭') {
  return {
    id: String(h.id || h.familyId || Date.now()),  // 强制字符串ID
    name: h.name || h.familyName || fallbackName,
    address: h.address || '',
    rooms: typeof h.rooms === 'number' ? h.rooms : (h.roomCount || 0),
    roomCount: typeof h.roomCount === 'number' ? h.roomCount : (h.rooms || 0),
    members: typeof h.members === 'number' ? h.members : (h.memberCount || 0),
    createdAt: h.createdAt || Date.now(),
    slideX: 0   // UI 用，默认0，避免渲染报错
  }
}
