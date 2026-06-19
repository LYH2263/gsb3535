import { http } from "../../services/http";

const state = () => ({
  myReservations: [],
  allReservations: [],
  loading: false
});

const getters = {
  myReservations: (state) => state.myReservations,
  allReservations: (state) => state.allReservations,
  loading: (state) => state.loading
};

const mutations = {
  setLoading(state, loading) {
    state.loading = loading;
  },
  setMyReservations(state, reservations) {
    state.myReservations = reservations;
  },
  setAllReservations(state, reservations) {
    state.allReservations = reservations;
  }
};

const actions = {
  async fetchMyReservations({ commit, rootGetters }) {
    commit("setLoading", true);
    try {
      const profile = rootGetters["auth/profile"];
      if (!profile) return;
      const res = await http.get(`/book-reservations/my/${profile.id}`);
      commit("setMyReservations", res.data);
    } finally {
      commit("setLoading", false);
    }
  },
  async fetchAllReservations({ commit }, keyword = "") {
    commit("setLoading", true);
    try {
      const params = keyword ? { keyword } : {};
      const res = await http.get("/book-reservations", { params });
      commit("setAllReservations", res.data);
    } finally {
      commit("setLoading", false);
    }
  },
  async reserve({ dispatch }, payload) {
    await http.post("/book-reservations", payload);
    await Promise.all([
      dispatch("fetchMyReservations"),
      dispatch("books/fetchBooks", null, { root: true })
    ]);
  },
  async cancelReservation({ dispatch }, id) {
    await http.post(`/book-reservations/${id}/cancel`);
    await Promise.all([
      dispatch("fetchMyReservations"),
      dispatch("fetchAllReservations")
    ]);
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
