import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SystemRoutingModule } from './system-routing.module';

import { SystemComponent } from './system.component';

@NgModule({
  declarations: [
    SystemComponent
  ],
  imports: [
    CommonModule,
    SystemRoutingModule
  ],
  exports: [
    SystemComponent
  ]
})
export class SystemModule { }
