import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SystemRoutingModule } from './system-routing.module';

import { SystemComponent } from './system.component';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';

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
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';

import { CommonModule as PrivateCommonModule } from 'src/app/common/common.module';
import { CreateSystemComponent } from './create-system/create-system.component';
import { DeleteSystemComponent } from './delete-system/delete-system.component';
import { UpdateSystemComponent } from './update-system/update-system.component';


@NgModule({
  imports: [
    CommonModule,
    SystemRoutingModule,
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
    NzInputNumberModule,
    PrivateCommonModule
  ],
  declarations: [
    SystemComponent,
    CreateSystemComponent, 
    DeleteSystemComponent, 
    UpdateSystemComponent
  ],
  exports: [
    SystemComponent
  ]
})
export class SystemModule { }
