import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-module-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './module-page.component.html',
  styleUrl: './module-page.component.scss'
})
export class ModulePageComponent {
  @Input() title = '';
  @Input() description = '';
  @Input() moduleCode = '';
  @Input() features: string[] = [];
}
