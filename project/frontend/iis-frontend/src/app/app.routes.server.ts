import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'content/new',
    renderMode: RenderMode.Client
  },
  {
    path: 'pricelists/create',
    renderMode: RenderMode.Client
  },
  {
    path: 'pricelists/create/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'pricelists/:id/edit',
    renderMode: RenderMode.Client
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
