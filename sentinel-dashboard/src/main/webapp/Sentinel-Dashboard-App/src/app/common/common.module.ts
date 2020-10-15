import { NgModule } from '@angular/core';
import { AppFilterPipe } from './pipe/app-filter.pipe';
import { FlowFilterPipe } from './pipe/flow-filter.pipe';
import { IdentityFilterPipe } from './pipe/identity-filter.pipe';
import { SystemFilterPipe } from './pipe/system-filter.pipe';

@NgModule({
  declarations: [
    AppFilterPipe, 
    FlowFilterPipe, 
    IdentityFilterPipe,
    SystemFilterPipe
  ],
  imports: [
  ],
  exports: [
    AppFilterPipe,
    FlowFilterPipe,
    IdentityFilterPipe,
    SystemFilterPipe
  ]
})
export class CommonModule { }