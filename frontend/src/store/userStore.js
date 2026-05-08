import { defineStore } from "pinia";
import request from "@/utils/request";

export const DEFAULT_AVATAR_URL =
  "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

const normalizeAvatarSrc = (raw) => {
  const s = String(raw ?? "").trim();
  if (!s) return "";
  const lowered = s.toLowerCase();
  if (lowered === "null" || lowered === "undefined") return "";
  return s;
};

const resolveAvatarSrc = (raw) => normalizeAvatarSrc(raw) || DEFAULT_AVATAR_URL;

export const useUserStore = defineStore("user", {
  state: () => ({
    userId: null,
    avatar: DEFAULT_AVATAR_URL,
    nickname: "用户",
  }),
  actions: {
    applyProfileUpdated(detail) {
      const avatar = detail?.avatar;
      const nickname = detail?.nickname;
      if (avatar != null) this.avatar = resolveAvatarSrc(avatar);
      if (nickname) this.nickname = nickname;
    },
    async fetchNavUser() {
      try {
        const data = await request.get("/profile/info");
        this.userId = data?.userId ?? null;
        this.avatar = resolveAvatarSrc(data?.avatar);
        this.nickname = data?.nickname || "用户";
        return data;
      } catch (error) {
        this.userId = null;
        this.avatar = DEFAULT_AVATAR_URL;
        this.nickname = "用户";
        return null;
      }
    },
  },
});
