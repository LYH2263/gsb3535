import { http } from "../../services/http";

const state = () => ({
  items: [],
  total: 0,
  loading: false
});

const getters = {
  books: (state) => state.items,
  total: (state) => state.total,
  loading: (state) => state.loading
};

const mutations = {
  setLoading(state, loading) {
    state.loading = loading;
  },
  setBooks(state, { items, total }) {
    state.items = items;
    state.total = total;
  }
};

const actions = {
  async fetchBooks({ commit }, params = {}) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/books", { params });
      commit("setBooks", {
        items: data.records,
        total: data.total
      });
    } finally {
      commit("setLoading", false);
    }
  },
  async createBook({ dispatch }, payload) {
    await http.post("/books", payload);
    dispatch("fetchBooks");
  },
  async updateBook({ dispatch }, payload) {
    await http.put(`/books/${payload.id}`, payload);
    dispatch("fetchBooks");
  },
  async deleteBook({ dispatch }, id) {
    await http.delete(`/books/${id}`);
    dispatch("fetchBooks");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
