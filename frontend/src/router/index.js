import { createRouter, createWebHistory } from "vue-router";
import store from "../store";
import LoginView from "../views/LoginView.vue";
import MainLayout from "../layouts/MainLayout.vue";
import BooksView from "../views/BooksView.vue";
import UsersView from "../views/UsersView.vue";
import BorrowsView from "../views/BorrowsView.vue";
import HistoryView from "../views/HistoryView.vue";
import OverdueView from "../views/OverdueView.vue";
import MyReservationsView from "../views/MyReservationsView.vue";
import ReservationManageView from "../views/ReservationManageView.vue";

const routes = [
  {
    path: "/login",
    name: "login",
    component: LoginView
  },
  {
    path: "/",
    component: MainLayout,
    redirect: "/books",
    children: [
      { path: "books", name: "books", component: BooksView },
      { path: "users", name: "users", component: UsersView, meta: { requiresAdmin: true } },
      { path: "borrows", name: "borrows", component: BorrowsView },
      { path: "history", name: "history", component: HistoryView, meta: { requiresAdmin: true } },
      { path: "overdue", name: "overdue", component: OverdueView, meta: { requiresAdmin: true } },
      { path: "my-reservations", name: "my-reservations", component: MyReservationsView },
      { path: "reservation-manage", name: "reservation-manage", component: ReservationManageView, meta: { requiresAdmin: true } }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const isAuth = store.getters["auth/isAuthenticated"];
  const profile = store.getters["auth/profile"];
  const isAdmin = profile?.role === "ADMIN";
  if (to.name !== "login" && !isAuth) {
    next({ name: "login" });
    return;
  }
  if (to.name === "login" && isAuth) {
    next({ name: "books" });
    return;
  }
  if (to.meta?.requiresAdmin && !isAdmin) {
    next({ name: "books" });
    return;
  }
  next();
});

export default router;
