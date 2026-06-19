import { http } from "../../services/http";

const state = () => ({
  items: [],
  loading: false
});

const getters = {
  users: (state) => state.items,
  loading: (state) => state.loading
};

const mutations = {
  setLoading(state, loading) {
    state.loading = loading;
  },
  setUsers(state, items) {
    state.items = items;
  }
};

const actions = {
  async fetchUsers({ commit }) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/users");
      commit("setUsers", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async createUser({ dispatch }, payload) {
    await http.post("/users", payload);
    dispatch("fetchUsers");
  },
  async updateUser({ dispatch }, payload) {
    await http.put(`/users/${payload.id}`, payload);
    dispatch("fetchUsers");
  },
  async deleteUser({ dispatch }, id) {
    await http.delete(`/users/${id}`);
    dispatch("fetchUsers");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
