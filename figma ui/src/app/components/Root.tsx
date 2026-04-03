import { Outlet } from "react-router";
import { BottomNav } from "./BottomNav";

export function Root() {
  return (
    <div className="flex flex-col h-screen max-w-md mx-auto bg-white overflow-hidden">
      <div className="flex-1 overflow-y-auto pb-20">
        <Outlet />
      </div>
      <BottomNav />
    </div>
  );
}
