import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'content/new',
    renderMode: RenderMode.Server
  },
  {
    path: 'pricelists/create',
    renderMode: RenderMode.Server
  },
  {
    path: 'pricelists/create/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'pricelists/:id/edit',
    renderMode: RenderMode.Server
  },
  {
    path: 'sales/customers/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'sales/processes/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'sales/contracts/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'adverse-effects/edit-report/:id',
    renderMode: RenderMode.Server
  },
  {
    path: 'adverse-effects/report/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
