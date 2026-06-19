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
  setMyReservations(state, records) {
    state.myReservations = records;
  },
  setAllReservations(state, records) {
    state.allReservations = records;
  }
};

const actions = {
  async fetchMyReservations({ commit }) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/book-reservations/my-reservations");
      commit("setMyReservations", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async fetchAllReservations({ commit }, keyword) {
    commit("setLoading", true);
    try {
      const params = keyword ? { keyword } : {};
      const { data } = await http.get("/book-reservations", { params });
      commit("setAllReservations", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async createReservation({ dispatch, rootGetters }, payload) {
    const profile = rootGetters["auth/profile"];
    const body = { ...payload, userId: payload.userId || profile?.id };
    await http.post("/book-reservations", body);
    await dispatch("fetchMyReservations");
  },
  async cancelReservation({ dispatch, rootGetters }, id) {
    await http.post(`/book-reservations/${id}/cancel`);
    const profile = rootGetters["auth/profile"];
    if (profile?.role === "ADMIN") {
      await dispatch("fetchAllReservations");
    } else {
      await dispatch("fetchMyReservations");
    }
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
