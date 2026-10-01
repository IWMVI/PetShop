import { Provider } from '@angular/core';
import {
  CalendarClock,
  CalendarX,
  Eye,
  History,
  IdCard,
  LUCIDE_ICONS,
  LucideIconProvider,
  Menu,
  PawPrint,
  Pencil,
  Plus,
  Search,
  Trash2,
  Users,
  Wrench,
} from 'lucide-angular';

/** Ícones Lucide usados na aplicação, disponíveis via <lucide-icon name="...">. */
export function provideAppIcons(): Provider {
  return {
    provide: LUCIDE_ICONS,
    multi: true,
    useValue: new LucideIconProvider({
      CalendarClock,
      CalendarX,
      Eye,
      History,
      IdCard,
      Menu,
      PawPrint,
      Pencil,
      Plus,
      Search,
      Trash2,
      Users,
      Wrench,
    }),
  };
}
