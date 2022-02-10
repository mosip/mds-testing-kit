# SBITestUi

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 9.0.1.

Change url to point to spring boot server in environments/environment.ts file. Currently it is set to base_url: 'http://localhost:8081/'

NodeJs is prerequisite

## Local Set up one time

Run `npm install` to download all the required configuration. `node_modules` will be created.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Since it is added inside java project and deployed together as one build, follow below step.

In 'environment.prod.ts' make below changes and revert after build is done.
base_url: 'http://test.mosip.io/phase2/',

In 'index.html' make below changes and revert after build is done.
 <base href="/phase2/">

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.
Run `ng build --prod`
Files inside `dist/` should be trasfered to mosip device service 

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
