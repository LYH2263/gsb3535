import { http } from "../../services/http";

const state = () => ({
  records: [],
  history: [],
  overdue: [],
  loading: false
});

const getters = {
  records: (state) => state.records,
  history: (state) => state.history,
  overdue: (state) => state.overdue,
  loading: (state) => state.loading
};

const mutations = {
  setLoading(state, loading) {
    state.loading = loading;
  },
  setRecords(state, records) {
    state.records = records;
  },
  setHistory(state, history) {
    state.history = history;
  },
  setOverdue(state, overdue) {
    state.overdue = overdue;
  }
};

const actions = {
  async fetchAll({ commit, rootGetters }) {
    commit("setLoading", true);
    try {
      const profile = rootGetters["auth/profile"];
      const endpoint = profile?.role === "ADMIN" ? "/borrows" : `/borrows/user/${profile.id}`;
      const { data } = await http.get(endpoint);
      commit("setRecords", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async fetchHistory({ commit }) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/borrows/history");
      commit("setHistory", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async fetchOverdue({ commit }) {
    commit("setLoading", true);
    try {
      const { data } = await http.get("/borrows/overdue");
      commit("setOverdue", data);
    } finally {
      commit("setLoading", false);
    }
  },
  async borrow({ dispatch }, payload) {
    await http.post("/borrows", payload);
    dispatch("fetchAll");
  },
  async returnBook({ dispatch }, payload) {
    await http.post("/borrows/return", payload);
    dispatch("fetchAll");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
