import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { AdminUsersPageComponent } from './pages/admin-users/admin-users-page.component';
import { RoleLandingComponent } from './pages/role-landing/role-landing.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: 'login', component: LoginComponent, title: 'IIS Drug CRM | Login' },
	{
		path: 'admin/users',
		component: AdminUsersPageComponent,
		canActivate: [authGuard],
		data: { roles: ['ROLE_ADMIN'] },
		title: 'IIS Drug CRM | Users',
	},
	{
		path: 'content',
		component: RoleLandingComponent,
		canActivate: [authGuard],
		data: {
			roles: ['ROLE_PRICELIST_CREATOR'],
			title: 'Content workspace',
			subtitle: 'Pricelist owner landing page.',
		},
		title: 'IIS Drug CRM | Content',
	},
	{
		path: 'published-pricelists',
		component: RoleLandingComponent,
		canActivate: [authGuard],
		data: {
			roles: ['ROLE_BUYER'],
			title: 'Published pricelists',
			subtitle: 'Buyer landing page.',
		},
		title: 'IIS Drug CRM | Pricelists',
	},
	{ path: '**', redirectTo: 'login' },
];
