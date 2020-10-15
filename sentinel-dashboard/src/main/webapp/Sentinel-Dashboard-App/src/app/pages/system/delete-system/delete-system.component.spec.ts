import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteSystemComponent } from './delete-system.component';

describe('DeleteSystemComponent', () => {
  let component: DeleteSystemComponent;
  let fixture: ComponentFixture<DeleteSystemComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteSystemComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteSystemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
