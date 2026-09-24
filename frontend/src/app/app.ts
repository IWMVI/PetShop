import { Component, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, LucideAngularModule, NzLayoutModule, NzMenuModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly recolhido = signal(false);

  protected readonly menu = [
    { rota: '/tutores', rotulo: 'Tutores', icone: 'users' },
    { rota: '/servicos', rotulo: 'Serviços', icone: 'wrench' },
  ];
}
