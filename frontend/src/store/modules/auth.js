import { http } from "../../services/http";

const state = () => ({
  token: localStorage.getItem("library_token") || "",
  profile: JSON.parse(localStorage.getItem("library_profile") || "null")
});

const getters = {
  token: (state) => state.token,
  profile: (state) => state.profile,
  isAuthenticated: (state) => Boolean(state.token)
};

const mutations = {
  setAuth(state, payload) {
    state.token = payload.token;
    state.profile = payload.profile;
  },
  clearAuth(state) {
    state.token = "";
    state.profile = null;
  }
};

const actions = {
  async login({ commit }, payload) {
    const { data } = await http.post("/auth/login", payload);
    const profile = {
      id: data.id,
      username: data.username,
      fullName: data.fullName,
      role: data.role
    };
    localStorage.setItem("library_token", data.token);
    localStorage.setItem("library_profile", JSON.stringify(profile));
    commit("setAuth", { token: data.token, profile });
  },
  logout({ commit }) {
    localStorage.removeItem("library_token");
    localStorage.removeItem("library_profile");
    commit("clearAuth");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
