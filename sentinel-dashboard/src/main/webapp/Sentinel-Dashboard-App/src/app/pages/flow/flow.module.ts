import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { FlowRoutingModule } from './flow-routing.module';

import { CreateFlowComponent } from './create-flow/create-flow.component';
import { DeleteFlowComponent } from './delete-flow/delete-flow.component';
import { UpdateFlowComponent } from './update-flow/update-flow.component';
import { FlowComponent } from './flow.component';

import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzRadioModule } from 'ng-zorro-antd/radio';
import { NzSwitchModule } from 'ng-zorro-antd/switch';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzAutocompleteModule } from 'ng-zorro-antd/auto-complete';
import { NzMessageModule } from 'ng-zorro-antd/message';

import { CommonModule as PrivateCommonModule } from 'src/app/common/common.module';

@NgModule({
  imports: [
    CommonModule,
    FlowRoutingModule,
    NzIconModule,
    NzButtonModule,
    NzTableModule,
    NzDividerModule,
    NzModalModule,
    NzFormModule,
    FormsModule,
    ReactiveFormsModule,
    NzInputModule,
    NzRadioModule,
    NzSwitchModule,
    NzGridModule,
    NzSelectModule,
    NzAutocompleteModule,
    NzMessageModule,
    PrivateCommonModule
  ],
  declarations: [
    FlowComponent, 
    CreateFlowComponent, 
    DeleteFlowComponent, 
    UpdateFlowComponent
  ],
  exports: [
    FlowComponent
  ]
})
export class FlowModule { }
