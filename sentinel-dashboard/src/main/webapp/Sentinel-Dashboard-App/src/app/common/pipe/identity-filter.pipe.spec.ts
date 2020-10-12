import { IdentityFilterPipe } from './identity-filter.pipe';

describe('IdentityFilterPipe', () => {
  it('create an instance', () => {
    const pipe = new IdentityFilterPipe();
    expect(pipe).toBeTruthy();
  });
});
