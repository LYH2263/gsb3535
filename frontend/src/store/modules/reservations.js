import { http } from "../../services/http";

const state = () => ({
  myReservations: [],
  adminReservations: [],
  loading: false,
  filters: {
    bookKeyword: "",
    userKeyword: ""
  }
});

const getters = {
  myReservations: (state) => state.myReservations,
  adminReservations: (state) => state.adminReservations,
  loading: (state) => state.loading,
  filters: (state) => state.filters
};

const mutations = {
  setLoading(state, loading) {
    state.loading = loading;
  },
  setMyReservations(state, list) {
    state.myReservations = list || [];
  },
  setAdminReservations(state, list) {
    state.adminReservations = list || [];
  },
  setFilters(state, filters) {
    state.filters = { ...state.filters, ...filters };
  },
  resetFilters(state) {
    state.filters = { bookKeyword: "", userKeyword: "" };
  }
};

const actions = {
  async fetchMyReservations({ commit, rootGetters }) {
    const profile = rootGetters["auth/profile"];
    if (!profile?.id) {
      commit("setMyReservations", []);
      return;
    }
    commit("setLoading", true);
    try {
      const { data } = await http.get(`/book-reservations/user/${profile.id}`);
      commit("setMyReservations", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async fetchAdminReservations({ commit, state }) {
    commit("setLoading", true);
    try {
      const params = {};
      if (state.filters.bookKeyword) params.bookKeyword = state.filters.bookKeyword;
      if (state.filters.userKeyword) params.userKeyword = state.filters.userKeyword;
      const { data } = await http.get("/book-reservations", { params });
      commit("setAdminReservations", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async createReservation({ dispatch }, payload) {
    await http.post("/book-reservations", payload);
    await dispatch("fetchMyReservations");
  },
  async cancelReservation({ dispatch, rootGetters }, id) {
    await http.delete(`/book-reservations/${id}`);
    const profile = rootGetters["auth/profile"];
    if (profile?.role === "ADMIN") {
      await dispatch("fetchAdminReservations");
    } else {
      await dispatch("fetchMyReservations");
    }
  },
  applyFilters({ commit, dispatch }, filters) {
    commit("setFilters", filters || {});
    return dispatch("fetchAdminReservations");
  },
  clearFilters({ commit, dispatch }) {
    commit("resetFilters");
    return dispatch("fetchAdminReservations");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
