import { createStore } from "vuex";
import auth from "./modules/auth";
import books from "./modules/books";
import users from "./modules/users";
import borrows from "./modules/borrows";
import ui from "./modules/ui";

export default createStore({
  modules: {
    auth,
    books,
    users,
    borrows,
    ui
  }
});
