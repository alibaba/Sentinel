import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'degradeFilter'
})
export class DegradeFilterPipe implements PipeTransform {
  transform(value: any, ...args: any[]): any {
    var res = [];
    if (args.length && args[0] !== undefined && args[0] !== "") {
      value.map(ele => {
        if (ele.resource.toLowerCase().includes(args[0].toLowerCase())) {
          res.push(ele);
        }
      });
      return res;
    } else {
      return value;
    }
  }
}
