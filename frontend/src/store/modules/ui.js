const state = () => ({
  globalError: ""
});

const getters = {
  globalError: (state) => state.globalError
};

const mutations = {
  setGlobalError(state, message) {
    state.globalError = message;
  },
  clearGlobalError(state) {
    state.globalError = "";
  }
};

const actions = {
  setGlobalError({ commit }, message) {
    commit("setGlobalError", message);
  },
  clearGlobalError({ commit }) {
    commit("clearGlobalError");
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
