# EdX Android Pull Request Guidelines

## Submitting a PR

### Before you create the PR
- Do the tests pass locally? (TODO add link to testing)
- Did you rebase against master? (TODO How to rebase)

### Creating the PR
Open a new pull request against the edx-app-android repository. The title of the PR should describe the feature, fix, or change that is being made. The description should have information that is relavent to anyone reviewing the PR.

Example template below. Some of these items may not be relavent for every PR, for example, an open source contributor external to edX may not be using our JIRA ticketing system and will not have a JIRA ticket number.

```
### Description

[TNL-XXXX](https://openedx.atlassian.net/browse/TNL-XXXX)

Add a description of your changes here.

### Notes
- Example: Use example.sandbox.edx.org to test against
- Example: This PR will not address x,y, and z, because of a, b, and c.
- Example: Feature is behind a flag
- Example: Why I didn't add any tests

### Testing
- [ ] i18n
- [ ] RTL
- [ ] safecommit shows 0 violations
- [ ] Unit, integration, acceptance tests as appropriate
- [ ] Analytics
- [ ] Performance
- [ ] Database migrations are backwards-compatible

### Reviewers
If you've been tagged for review, please check your corresponding box once you've given the :+1:.
- [ ] Code review: @Reviewer1
- [ ] Code review: @Reviewer2
- [ ] UI strings/error msgs review: @...
- [ ] UX review: @...
- [ ] Accessibility review: @...
- [ ] Product review: @...
- [ ] FYI @someone
```

#### 2 Reviewers for a Pull Request
When creating a pull request against the edx-app-android repository, 2 reviewers will review the code and give a thumbs up (:+1:) when the PR is ready to merge. Depending on the PR, only 1 may be necessary such as fixing a typo in a string or removing a print statement.

#### Getting reviewed
As your PR is being reviewed, there will be comments added to the PR. When you address these comments and there are code changes required, push a new commit with the changes such as "PR fixup, removed commented out code". This makes it easy for the reviewer to see the new changes. These commits will be squashed before being merged. When you are ready for another pass, feel free to ping your reviewers in a new comment.

Sometimes you might get a comment that is out of scope such as an auto-format showing a line of code that could be problematic but unrelated. Feel free to discuss with the reviewer.

#### Rebasing
There different ways to update your branch with the current master branch. The preferred way is to rebase against master rather than erging while PR comments are being addressed. Rebase often! When you are ready to merge, there will be no conflicts since they have already been resolved when rebasing.

#### Merging!
Commits can get messy so we want to squash all of our commits into logical units. Usually this means submitting one commit per PR but sometimes multiple somewhat related changes may be made in one pull request. For exameple, a feature depends on another area of the code to be refactored. Whether the feature is implemented or not, the refactoring can be considered a sepearate commit.

- Two thumbs up from 2 sepearate reviewers :+1:
- Rebased (preferred) or merge against master branch (TODO rebasing)
- Squashed commits
- Commit message properly describes the change.
- Addressed all outstanding comments

## TODO Reviewing a PR
When in doubt, bring it up or refer to the AOSP guidelines.
#### Run the code!
Check if the code being changed in the PR works.
#### Code Style
##### Naming Conventions
##### Syntax
#### TODO Tests
Are there tests?
#### TODO Accessiblity

