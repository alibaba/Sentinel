import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteFlowComponent } from './delete-flow.component';

describe('DeleteFlowComponent', () => {
  let component: DeleteFlowComponent;
  let fixture: ComponentFixture<DeleteFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
