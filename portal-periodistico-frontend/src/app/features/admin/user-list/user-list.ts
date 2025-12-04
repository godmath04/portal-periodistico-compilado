import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AdminUserService } from '../../../core/services/admin-user.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserResponse, AdminUserRequest } from '../../../core/models/admin-user.model';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './user-list.html',
  styleUrl: './user-list.scss',
})
export class UserList implements OnInit {
  users: UserResponse[] = [];
  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // Modal state
  showModal: boolean = false;
  isEditMode: boolean = false;
  selectedUser: UserResponse | null = null;

  // Form data
  formData: AdminUserRequest = {
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    roles: [],
  };

  availableRoles: string[] = [];

  constructor(
    private adminUserService: AdminUserService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.availableRoles = this.adminUserService.getAvailableRoles();
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminUserService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando usuarios:', error);
        this.errorMessage = 'Error al cargar los usuarios';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedUser = null;
    this.formData = {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      roles: [],
    };
    this.showModal = true;
  }

  openEditModal(user: UserResponse): void {
    this.isEditMode = true;
    this.selectedUser = user;
    this.formData = {
      username: user.username,
      email: user.email,
      password: '', // Don't pre-fill password
      firstName: user.firstName,
      lastName: user.lastName,
      roles: [...user.roles],
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedUser = null;
    this.formData = {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      roles: [],
    };
  }

  toggleRole(role: string): void {
    const index = this.formData.roles.indexOf(role);
    if (index > -1) {
      this.formData.roles.splice(index, 1);
    } else {
      this.formData.roles.push(role);
    }
  }

  isRoleSelected(role: string): boolean {
    return this.formData.roles.includes(role);
  }

  submitForm(): void {
    // Validation
    if (
      !this.formData.username ||
      !this.formData.email ||
      !this.formData.firstName ||
      !this.formData.lastName
    ) {
      this.errorMessage = 'Por favor completa todos los campos obligatorios';
      return;
    }

    if (!this.isEditMode && !this.formData.password) {
      this.errorMessage = 'La contraseña es obligatoria para nuevos usuarios';
      return;
    }

    if (this.formData.roles.length === 0) {
      this.errorMessage = 'Debes seleccionar al menos un rol';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const request: AdminUserRequest = {
      ...this.formData,
      password: this.isEditMode && !this.formData.password ? undefined : this.formData.password,
    };

    const operation = this.isEditMode
      ? this.adminUserService.updateUser(this.selectedUser!.userId, request)
      : this.adminUserService.createUser(request);

    operation.subscribe({
      next: (user) => {
        this.successMessage = this.isEditMode
          ? `Usuario ${user.username} actualizado exitosamente`
          : `Usuario ${user.username} creado exitosamente`;
        this.closeModal();
        this.loadUsers();
      },
      error: (error) => {
        console.error('Error guardando usuario:', error);
        this.errorMessage = error.error || 'Error al guardar el usuario';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  toggleUserStatus(user: UserResponse): void {
    const action = user.active ? 'desactivar' : 'activar';
    const actionPast = user.active ? 'desactivado' : 'activado';

    if (confirm(`¿Estás seguro de ${action} al usuario ${user.username}?`)) {
      this.loading = true;
      this.errorMessage = '';

      if (user.active) {
        this.adminUserService.deleteUser(user.userId).subscribe({
          next: () => {
            this.successMessage = `Usuario ${user.username} ${actionPast} exitosamente`;
            this.loadUsers();
          },
          error: (error) => {
            console.error('Error cambiando estado del usuario:', error);
            this.errorMessage = error.error || 'Error al cambiar el estado del usuario';
            this.loading = false;
            this.cdr.detectChanges();
          },
        });
      } else {
        const reactivateRequest: AdminUserRequest = {
          username: user.username,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          roles: user.roles,
          active: true,
        };

        this.adminUserService.updateUser(user.userId, reactivateRequest).subscribe({
          next: () => {
            this.successMessage = `Usuario ${user.username} ${actionPast} exitosamente`;
            this.loadUsers();
          },
          error: (error) => {
            console.error('Error cambiando estado del usuario:', error);
            this.errorMessage = error.error || 'Error al cambiar el estado del usuario';
            this.loading = false;
            this.cdr.detectChanges();
          },
        });
      }
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
