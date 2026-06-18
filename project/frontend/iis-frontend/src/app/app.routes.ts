import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { passwordChangeGuard } from './core/auth/password-change.guard';
import { LoginComponent } from './pages/login/login.component';
import { AdminUsersPageComponent } from './pages/admin-users/admin-users-page.component';
import { TeamManagementComponent } from './pages/team-management/team-management.component';
import { AdminRegionsPageComponent } from './pages/admin-regions/admin-regions-page.component';
import { RoleLandingComponent } from './pages/role-landing/role-landing.component';
import { ForcePasswordChangeComponent } from './pages/force-password-change/force-password-change.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { PortfolioPageComponent } from './pages/portfolio/portfolio-page.component';
import { PricelistCreateComponent } from './pages/pricelist-create/pricelist-create.component';
import { SalesDashboardComponent } from './pages/sales-dashboard/sales-dashboard.component';
import { BuyerCatalogComponent } from './pages/buyer-catalog/buyer-catalog.component';
import { LeadsListComponent } from './features/sales/pages/leads-list/leads-list.component';
import { CustomersListComponent } from './features/sales/pages/customers-list/customers-list.component';
import { ProcessesListComponent } from './features/sales/pages/processes-list/processes-list.component';
import { CommunicationsListComponent } from './features/sales/pages/communications-list/communications-list.component';

export const routes: Routes = [
    { path: '', pathMatch: 'full', redirectTo: 'login' },
    { path: 'login', component: LoginComponent, title: 'IIS Drug CRM | Login' },
    {
        path: 'force-password-change',
        component: ForcePasswordChangeComponent,
        canActivate: [authGuard],
        title: 'IIS Drug CRM | Force Password Change',
    },
    {
        path: 'admin/users',
        component: AdminUsersPageComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_ADMIN'] },
        title: 'IIS Drug CRM | Admin CRUD Panel',
    },
    {
        path: 'admin/regions',
        component: AdminRegionsPageComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_ADMIN'] },
        title: 'IIS Drug CRM | Regions',
    },
    {
        path: 'profile',
        component: ProfileComponent,
        canActivate: [authGuard, passwordChangeGuard],
        title: 'IIS Drug CRM | Profile',
    },
    {
        path: 'teams',
        component: TeamManagementComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_PRICELIST_CREATOR'] },
        title: 'IIS Drug CRM | Teams',
    },
    {
        path: 'content',
        component: RoleLandingComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: {
            roles: ['ROLE_PRICELIST_CREATOR'],
            title: 'Content workspace',
            subtitle: 'Pricelist owner landing page.',
        },
        title: 'IIS Drug CRM | Content',
    },
    {
        path: 'content/mine',
        loadComponent: () => import('./pages/pricelist-list/pricelist-list.component').then(m => m.PricelistListComponent),
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_PRICELIST_CREATOR'] },
        title: 'IIS Drug CRM | My Drafts',
    },
    {
        path: 'content/new',
        component: PricelistCreateComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_PRICELIST_CREATOR'] },
        title: 'IIS Drug CRM | New Pricelist',
    },
    {
        path: 'pricelists/:id/edit',
        component: PricelistCreateComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_PRICELIST_CREATOR'] },
        title: 'IIS Drug CRM | Edit Pricelist',
    },
    {
        path: 'published-pricelists',
        component: BuyerCatalogComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_BUYER'] },
        title: 'IIS Drug CRM | Pricelists',
    },
    {
        path: 'catalog',
        component: BuyerCatalogComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_BUYER'] },
        title: 'IIS Drug CRM | Medicine Catalog',
    },
    {
        path: 'portfolio',
        component: PortfolioPageComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: { roles: ['ROLE_PORTFOLIO_MANAGER'] },
        title: 'IIS Drug CRM | Product Portfolio',
    },
    {
        path: 'sales',
        component: SalesDashboardComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: {
            roles: ['ROLE_SALES_REPRESENTATIVE', 'ROLE_ACCOUNT_MANAGER', 'ROLE_SALES_MANAGER'],
        },
        title: 'IIS Drug CRM | Sales',
    },
    {
        path: 'sales/leads',
        component: LeadsListComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: {
            roles: [
            'ROLE_SALES_REPRESENTATIVE',
            'ROLE_ACCOUNT_MANAGER',
            'ROLE_SALES_MANAGER',
            ],
        },
        title: 'IIS Drug CRM | Leads',
    },
    {
        path: 'sales/customers',
        component: CustomersListComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: {
            roles: [
            'ROLE_SALES_REPRESENTATIVE',
            'ROLE_ACCOUNT_MANAGER',
            'ROLE_SALES_MANAGER',
            ],
        },
        title: 'IIS Drug CRM | Customers',
    },
    {
        path: 'sales/processes',
        component: ProcessesListComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: {
            roles: [
            'ROLE_SALES_REPRESENTATIVE',
            'ROLE_ACCOUNT_MANAGER',
            'ROLE_SALES_MANAGER',
            ],
        },
        title: 'IIS Drug CRM | Sales Pipeline',
    },
    {
        path: 'sales/communications',
        component: CommunicationsListComponent,
        canActivate: [authGuard, passwordChangeGuard],
        data: {
          roles: [
                'ROLE_SALES_REPRESENTATIVE',
                'ROLE_ACCOUNT_MANAGER',
                'ROLE_SALES_MANAGER',
          ],
        },
        title: 'IIS Drug CRM | Communications',
    },
    { path: '**', redirectTo: 'login' },
];
