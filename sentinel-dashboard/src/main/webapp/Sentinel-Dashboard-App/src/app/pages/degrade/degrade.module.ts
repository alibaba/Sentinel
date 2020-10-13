import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DegradeRoutingModule } from './degrade-routing.module';

import { DegradeComponent } from './degrade.component';

@NgModule({
  declarations: [
    DegradeComponent
  ],
  imports: [
    CommonModule,
    DegradeRoutingModule
  ],
  exports: [
    DegradeComponent
  ]
})
export class DegradeModule { }
