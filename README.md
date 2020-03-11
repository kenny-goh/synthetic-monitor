# synthetic-monitor

A simple tool for synthetic monitoring and testing using YAML files to orchestrate the tests. The main advantage of this approach is that you don't need to code custom scripts to run synthetic tests, instead you describe the tests declaratively using simple to understand YAML and let the synthetic-monitor engine do the heavy lifting. 

The synthetic-monitor YAML tests currently supports the following:
- Can be invoked via scheduler, directly via the API or the web-based front end 
- Tests can be loaded from files, and can be managed via API or the web-based frontend
- Each synthetic-monitor test can have one or more synthentic test actions 
- The following test actions are currently supported:
  - API calls (GET, POST, PUSH, DELETE) 

![Overview screen](screenshot1.png)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the software and how to install them

```
Give examples
```

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [SprintBoot](http://spring.org/) - Spring Boot 2.2.4
* [Maven](https://maven.apache.org/) - Dependency Management
* [ReactJS](http://reactjs.org/) - Used to build the front-end

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning


## Authors

* **Kenny Goh** - *Initial work* - [KG](https://github.com/Kenny-goh)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Todo


