import { NgModule } from '@angular/core';
import { AppFilterPipe } from './pipe/app-filter.pipe';
import { FlowFilterPipe } from './pipe/flow-filter.pipe';
import { IdentityFilterPipe } from './pipe/identity-filter.pipe';
import { SystemFilterPipe } from './pipe/system-filter.pipe';
import { DegradeFilterPipe } from './pipe/degrade-filter.pipe';

@NgModule({
  declarations: [
    AppFilterPipe, 
    FlowFilterPipe, 
    IdentityFilterPipe,
    SystemFilterPipe,
    DegradeFilterPipe
  ],
  imports: [
  ],
  exports: [
    AppFilterPipe,
    FlowFilterPipe,
    IdentityFilterPipe,
    SystemFilterPipe,
    DegradeFilterPipe
  ]
})
export class CommonModule { }