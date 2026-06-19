import { http } from "../../services/http";

const state = () => ({
  items: [],
  myItems: [],
  total: 0,
  loading: false
});

const getters = {
  items: (state) => state.items,
  myItems: (state) => state.myItems,
  total: (state) => state.total,
  loading: (state) => state.loading
};

const mutations = {
  setLoading(state, loading) {
    state.loading = loading;
  },
  setItems(state, { items, total }) {
    state.items = items;
    state.total = total;
  },
  setMyItems(state, items) {
    state.myItems = items;
  }
};

const actions = {
  async fetchReservations({ commit }, params = {}) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/book-reservations", { params });
      commit("setItems", {
        items: data.records,
        total: data.total
      });
    } finally {
      commit("setLoading", false);
    }
  },
  async fetchMyReservations({ commit }) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/book-reservations/my");
      commit("setMyItems", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async createReservation({ dispatch }, payload) {
    await http.post("/book-reservations", payload);
    dispatch("fetchMyReservations");
  },
  async cancelReservation({ dispatch }, id) {
    await http.post(`/book-reservations/${id}/cancel`);
    dispatch("fetchReservations");
    dispatch("fetchMyReservations");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
